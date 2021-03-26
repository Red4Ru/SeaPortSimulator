import java.util.ArrayList;
import java.util.List;

public class SeaPortSimulator {
    private static final Time absArrivalDeviation = new Time(24 * 7, 10);
    private static final Time unloadingDeviation = new Time(0, 1440);
    private Schedule initialSchedule;
    private List<Unload> actualSchedule;
    private Data endOfSimulation;

    public SeaPortSimulator(Schedule schedule) {
        setSchedule(schedule);
    }

    private void setSchedule(Schedule schedule) {
        Data maxData = new Data(0);
        initialSchedule = schedule;
        int nEvents = initialSchedule.getEventsNumber();
        actualSchedule = new ArrayList<>();
        for (int i = 0; i < nEvents; i++) {
            Unload unload = toActualEvent(initialSchedule.getNthEvent(i));
            if (unload != null) {
                actualSchedule.add(unload);
                maxData = new Data(Math.max(unload.getEndingData().toMinutes(), maxData.toMinutes()));
            }
        }
        endOfSimulation = maxData;
    }

    private Unload toActualEvent(ScheduleEvent event) {
        Data start = event.getStartingData();
        Data start_min = new Data(start.toMinutes() - absArrivalDeviation.toMinutes());
        Data start_max = new Data(Math.min(Schedule.endOfSchedule.toMinutes(),
                start.toMinutes() + absArrivalDeviation.toMinutes()));
        Data actualStart = new Data(Rand.genNormalInt(event.getStartingData().toMinutes(),
                start_min.toMinutes(), start_max.toMinutes()));
        if (actualStart.toMinutes() < 0) return null;
        Time extraTime = new Time(Math.abs(Rand.genNormalInt(0,
                -unloadingDeviation.toMinutes(), unloadingDeviation.toMinutes())));
        return new Unload(event, actualStart, extraTime);
    }

    public void printActualSchedule() {
        System.out.println(actualSchedule);
    }

    public void printOriginalSchedule() {
        System.out.println(initialSchedule);
    }

    public int simulate(int[] nUnloaders) {
        int nUnloadersTotal = 0;
        int totalPenalty = 0;
        int lastDayPrinted = 0;
        final int PENNY_PER_UNLOADER = 30000;
        final int PENNY_PER_HOUR = 100;
        final int n = CargoType.values().length;
        final long RESET_DELAY = 10;
        List<Unload> remainUnloads = new ArrayList<>(actualSchedule);
        Unloader.reset(RESET_DELAY);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < nUnloaders[i]; j++) {
                Thread thread = new Thread(new Unloader(CargoType.values()[i]));
                thread.start();
            }
            nUnloadersTotal += nUnloaders[i];
        }
        totalPenalty += nUnloadersTotal * PENNY_PER_UNLOADER;
        Data data = new Data(0);
        while ((data.toMinutes() < endOfSimulation.toMinutes()) || (Unloader.getAvailableUnloadsLen() > 0)) {
            if (data.getDay() != lastDayPrinted) {
                lastDayPrinted = data.getDay();
//                System.out.printf("Day: %d, Available unloads: %d\n",
//                        lastDayPrinted, Unloader.getAvailableUnloadsLen());
            }
            List<Unload> added = new ArrayList<>();
            for (Unload unload : remainUnloads) {
                if (unload.getStartingData().toMinutes() == data.toMinutes()) {
                    if (nUnloaders[unload.getCargoType().ordinal()] > 0) {
                        Unloader.addAvailableUnload(unload);
                    }
                    added.add(unload);
                }
            }
            remainUnloads.removeAll(added);
            Unloader.setCurrentData(data);
            while (Unloader.getNumberWaiting() < nUnloadersTotal) {
            }
            data = new Data(data.toMinutes() + 1);
        }
        totalPenalty += Unloader.getPenalty(PENNY_PER_HOUR);
        Unloader.reset(RESET_DELAY);
        return totalPenalty;
    }

    private double perShip(int penalty) {
        return (double) (penalty) / actualSchedule.size();
    }

    private int minimize(CargoType cargoType, double min_count, double max_count) {
        // gold proportion method
        int[] nUnloaders = new int[CargoType.values().length];
        for (CargoType cargoType1 : CargoType.values()) {
            nUnloaders[cargoType1.ordinal()] = 0;
        }
        final double FI = (3 - Math.sqrt(5)) / 2;
        int to_check;
        to_check = (int) Math.floor(min_count + (max_count - min_count) * FI);
        System.out.printf("(%s) a. Count: %d\n", cargoType, to_check);
        nUnloaders[cargoType.ordinal()] = to_check;
        double f0 = perShip(simulate(nUnloaders));
        System.out.printf("Penny per ship: %,.2f\n", f0);
        to_check = (int) Math.ceil(max_count - (max_count - min_count) * FI);
        System.out.printf("(%s) b. Count: %d\n", cargoType, to_check);
        nUnloaders[cargoType.ordinal()] = to_check;
        double f1 = perShip(simulate(nUnloaders));
        System.out.printf("Penny per ship: %,.2f\n", f1);
        int i = 0;
        while (max_count - min_count > 1) {
            if (f0 < f1) {
                max_count = max_count - (max_count - min_count) * FI;
                to_check = (int) Math.floor(min_count + (max_count - min_count) * FI);
                System.out.printf("(%s) %d. Count: %d\n", cargoType, i + 1, to_check);
                f1 = f0;
                nUnloaders[cargoType.ordinal()] = to_check;
                f0 = perShip(simulate(nUnloaders));
                System.out.printf("Penny per ship (f0): %,.2f\n", f0);
            } else if (f0 > f1) {
                min_count = min_count + (max_count - min_count) * FI;
                to_check = (int) Math.ceil(max_count - (max_count - min_count) * FI);
                System.out.printf("(%s) %d. Count: %d\n", cargoType, i + 1, to_check);
                f0 = f1;
                nUnloaders[cargoType.ordinal()] = to_check;
                f1 = perShip(simulate(nUnloaders));
                System.out.printf("Penny per ship (f1): %,.2f\n", f1);
            } else return minimize(cargoType, min_count, max_count);
            i++;
            System.out.printf("Min: %,.2f, Max: %,.2f\n", min_count, max_count);
            System.out.printf("f0: %,.2f, f1: %,.2f\n", f0, f1);
        }
        return to_check;
    }

    public static void main(String[] args) {
        final int[] ASSUMED_UL_COUNTS = new int[]{1, 1, 1};
        int[] UL_COUNTS = new int[]{0, 0, 0};
        int penny;
        final int max_count = 15;
        SeaPortSimulator seaPortSimulator;

        Schedule.main(ASSUMED_UL_COUNTS);
        seaPortSimulator = new SeaPortSimulator(JSONService.loadSchedule());
        System.out.printf("Total ships: %d\n", seaPortSimulator.actualSchedule.size());
        for (CargoType cargoType : CargoType.values()) {
            UL_COUNTS[cargoType.ordinal()] = seaPortSimulator.minimize(cargoType, 1, max_count);
        }
        penny = seaPortSimulator.simulate(UL_COUNTS);
        System.out.printf("Minimal penny when (%d,%d,%d): %d (%,.2f per ship)\n", UL_COUNTS[0], UL_COUNTS[1], UL_COUNTS[2],
                penny, (double) (penny) / seaPortSimulator.actualSchedule.size());

    }
}
