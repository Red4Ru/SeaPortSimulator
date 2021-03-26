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
                System.out.printf("New day: %d\n", lastDayPrinted);
            }
            List<Unload> added = new ArrayList<>();
            for (Unload unload : remainUnloads) {
                if (unload.getStartingData().toMinutes() == data.toMinutes()) {
                    Unloader.addAvailableUnload(unload);
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
        System.out.printf("Total ships: %d\n", actualSchedule.size());
        Unloader.reset(RESET_DELAY);
        return totalPenalty;
    }

    public static void main(String[] args) {
        int[] UL_COUNTS;
        int penny;
        SeaPortSimulator seaPortSimulator;

        UL_COUNTS = new int[]{5, 5, 5};
        Schedule.main(UL_COUNTS);
        seaPortSimulator = new SeaPortSimulator(JSONService.loadSchedule());
        penny = seaPortSimulator.simulate(UL_COUNTS);
        System.out.printf("Penny when (%d,%d,%d): %d (%,.2f per ship)\n", UL_COUNTS[0], UL_COUNTS[1], UL_COUNTS[2],
                penny, (double) (penny) / seaPortSimulator.actualSchedule.size());

    }
}
