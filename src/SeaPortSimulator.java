import java.util.ArrayList;
import java.util.List;

public class SeaPortSimulator {
    private static final Time absArrivalDeviation = new Time(24 * 7, 10);
    private static final Time unloadingDeviation = new Time(0, 1440);
    private List<Unload> actualSchedule;
    private Data endOfSimulation;

    public SeaPortSimulator(Schedule schedule) {
        setSchedule(schedule);
    }

    public void setSchedule(Schedule schedule) {
        Data maxData = new Data(0);
        int nEvents = schedule.getEventsNumber();
        actualSchedule = new ArrayList<>();
        for (int i = 0; i < nEvents; i++) {
            Unload unload = toActualEvent(schedule.getNthEvent(i));
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
        Data start_max = new Data(start.toMinutes() + absArrivalDeviation.toMinutes());
        Data actualStart = new Data(Rand.genNormalInt(event.getStartingData().toMinutes(),
                start_min.toMinutes(), start_max.toMinutes()));
        if (actualStart.toMinutes() < 0) return null;
        Time extraTime = new Time(Math.abs(Rand.genNormalInt(0,
                -unloadingDeviation.toMinutes(), unloadingDeviation.toMinutes())));
        return new Unload(event, actualStart, extraTime);
    }

    public int simulate(int[] nUnloaders, boolean needReport) {
        int nUnloadersTotal = 0;
        int totalPenalty = 0;
        int totalUnloadQueueLength = 0;
        final int PENNY_PER_UNLOADER = 30000;
        final int PENNY_PER_HOUR = 100;
        final int n = CargoType.values().length;
        final long RESET_DELAY = 10;
        List<Unload> remainUnloads = new ArrayList<>();
        for (Unload unload : actualSchedule) {
            remainUnloads.add(new Unload(unload));
        }
        SeaPort port = new SeaPort();
        port.reset(RESET_DELAY);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < nUnloaders[i]; j++) {
                Thread thread = new Thread(new Unloader(port, CargoType.values()[i]));
                thread.start();
            }
            nUnloadersTotal += nUnloaders[i];
        }
        totalPenalty += nUnloadersTotal * PENNY_PER_UNLOADER;
        Data data = new Data(0);
        while ((data.toMinutes() < endOfSimulation.toMinutes()) || (port.getAvailableUnloadsLen() > 0)) {
            List<Unload> added = new ArrayList<>();
            for (Unload unload : remainUnloads) {
                if (unload.getStartingData().toMinutes() == data.toMinutes()) {
                    if (nUnloaders[unload.getCargoType().ordinal()] > 0) {
                        port.addAvailableUnload(unload);
                    }
                    added.add(unload);
                }
            }
            remainUnloads.removeAll(added);
            port.setCurrentData(data);
            totalUnloadQueueLength += port.getAvailableUnloadsLen();
            data = new Data(data.toMinutes() + 1);
        }
        totalPenalty += port.getPenalty(PENNY_PER_HOUR);
        if (needReport) {
            Unload[] carriedUnloads = port.getCarriedUnloads();
            int i = 0;
            int totalWaiting = 0;
            int minExcess = -1;
            int maxExcess = 0;
            for (Unload unload : carriedUnloads) {
                i++;
                System.out.printf("%d) %s\n\n", i, unload);
                totalWaiting += unload.getWaitingTime().toMinutes();
                int excess = unload.getExcess().toMinutes();
                if ((minExcess == -1) || (minExcess > excess)) {
                    minExcess = excess;
                }
                if (maxExcess < excess) {
                    maxExcess = excess;
                }
            }
            System.out.printf("\nTotal ships: %d", actualSchedule.size());
            System.out.printf("\nMean unload queue length: %d", totalUnloadQueueLength / data.toMinutes());
            System.out.printf("\nMean unload waiting time: %s", new Time(totalWaiting / carriedUnloads.length));
            System.out.printf("\nMin excess time: %s", new Time(minExcess));
            System.out.printf("\nMax excess time: %s", new Time(maxExcess));
            System.out.println();
        }
        port.reset(RESET_DELAY);
        return totalPenalty;
    }

    public int[] findOptimalUnloaderCounts() {
        int N_ITERS = 2;
        int min_count = 1;
        int[] nUnloaders = new int[CargoType.values().length];
        for (CargoType cargoType : CargoType.values()) {
            nUnloaders[cargoType.ordinal()] = 0;
        }
        for (CargoType cargoType : CargoType.values()) {
            int bestCount = -1;
            double bestPenalty = -1;
            for (int i = min_count; ; i++) {
                nUnloaders[cargoType.ordinal()] = i;
                double meanPenalty = 0;
                for (int j = 0; j < N_ITERS; j++) {
                    meanPenalty += (double) simulate(nUnloaders, false) / actualSchedule.size();
                }
                meanPenalty /= N_ITERS;
                System.out.printf("Type: %s, Count: %d, Penalty: %,.2f\n", cargoType, i, meanPenalty);
                if (bestPenalty < meanPenalty && bestPenalty != -1) {
                    break;
                }
                bestCount = i;
                bestPenalty = meanPenalty;
            }
            nUnloaders[cargoType.ordinal()] = bestCount;
            System.out.printf("Type: %s, Best Count: %d, Best Penalty: %,.2f\n", cargoType, bestCount, bestPenalty);
        }
        return nUnloaders;
    }

    public static void main(String[] args) {
        final int[] ASSUMED_UL_COUNTS = new int[]{1, 1, 1};
        int penalty;
        SeaPortSimulator seaPortSimulator;

        Schedule.main(ASSUMED_UL_COUNTS);
        seaPortSimulator = new SeaPortSimulator(JSONService.loadSchedule());
        System.out.println("Starting simulation...");
        int[] nUnloaders = seaPortSimulator.findOptimalUnloaderCounts();
        penalty = seaPortSimulator.simulate(nUnloaders, true);
        System.out.printf("Minimal penalty when (%d,%d,%d)\n",
                nUnloaders[0], nUnloaders[1], nUnloaders[2]);
        System.out.printf("Minimal penalty: %d (%,.2f per ship)\n",
                penalty, (double) penalty / seaPortSimulator.actualSchedule.size());
    }
}
