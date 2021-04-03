import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class SortByEndingTime implements Comparator<Unload> {
    @Override
    public int compare(Unload o1, Unload o2) {
        return o1.getEndingData().toMinutes() - o2.getEndingData().toMinutes();
    }
}

public class SeaPort {
    public static final int MAX_SAME_TIME = 2, DEFAULT_CURRENT_DATA = -1;
    private final PriorityQueue<Unload>[] availableUnloads = new PriorityQueue[CargoType.values().length];
    private final PriorityQueue<Unload> carriedUnloads = new PriorityQueue<>(new SortByEndingTime());
    private final Object[] mutexes = new Object[CargoType.values().length];
    private final Object totalDelayMutex = new Object();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicInteger nWaiting = new AtomicInteger(0);
    private final AtomicInteger nUnloadersTotal = new AtomicInteger(0);
    private Data currentData;
    private Time totalDelay;

    public SeaPort() {
        this.currentData = new Data(DEFAULT_CURRENT_DATA);
        this.totalDelay = new Time(0);
    }

    public void incrementnUnloadersTotal() {
        nUnloadersTotal.incrementAndGet();
    }

    public void decrementnUnloadersTotal() {
        nUnloadersTotal.decrementAndGet();
    }

    public void incrementnWaiting() {
        nWaiting.incrementAndGet();
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isCurrentData(int dataInMinutes) {
        return dataInMinutes == currentData.toMinutes();
    }

    public Data work(Unloader unloader) {
        Data endOfTask = null;
        CargoType cargoType = unloader.getCargoType();
        PriorityQueue<Unload> checked = new PriorityQueue<>(new SortByEndingTime());
        synchronized (mutexes[cargoType.ordinal()]) {
            while (!availableUnloads[cargoType.ordinal()].isEmpty()) {
                Unload unload = availableUnloads[cargoType.ordinal()].poll();
                int involvedUnloaders = unload.getSimultaneousExecutesCount();
                if (involvedUnloaders < MAX_SAME_TIME) {
                    endOfTask = unload.execute();
                    if (unload.getRemainingTime().toMinutes() == 0) {
                        synchronized (totalDelayMutex) {
                            totalDelay = new Time((unload.getExcess().getHours() + totalDelay.getHours())
                                    * Time.MM_IN_HH);
                            carriedUnloads.add(unload);
                        }
                    } else {
                        checked.add(unload);
                    }
                    break;
                }
                checked.add(unload);
            }
            availableUnloads[cargoType.ordinal()].addAll(checked);
        }
        return endOfTask;
    }

    public synchronized void addAvailableUnload(Unload unload) {
        availableUnloads[unload.getCargoType().ordinal()].add(new Unload(unload));
    }

    public synchronized Unload[] getCarriedUnloads() {
        Unload[] result = new Unload[carriedUnloads.size()];
        int i = 0;
        for (Unload unload : carriedUnloads) {
            result[i] = new Unload(unload);
            i++;
        }
        return result;
    }

    public void setCurrentData(Data currentData) {
        synchronized (this) {
            nWaiting.set(0);
            this.currentData = new Data(currentData);
            for (CargoType cargoType : CargoType.values()) {
                for (Unload unload : availableUnloads[cargoType.ordinal()]) {
                    unload.setCurrentData(this.currentData);
                }
            }
            this.notifyAll();
        }
        while (this.getNumberWaiting() < nUnloadersTotal.get()) {
        }
    }

    public synchronized int getPenalty(int pennyPerHour) {
        synchronized (totalDelayMutex) {
            return pennyPerHour * totalDelay.getHours();
        }
    }

    public synchronized void reset(long delay) {
        running.set(false);
        this.notifyAll();
        try {
            this.wait(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        running.set(true);
        currentData = new Data(DEFAULT_CURRENT_DATA);
        synchronized (totalDelayMutex) {
            totalDelay = new Time(0);
        }
        for (CargoType cargoType : CargoType.values()) {
            availableUnloads[cargoType.ordinal()] = new PriorityQueue<>(new SortByEndingTime());
            mutexes[cargoType.ordinal()] = new Object();
        }
        carriedUnloads.clear();
        nWaiting.set(0);
    }

    public synchronized int getAvailableUnloadsLen() {
        int sum = 0;
        for (CargoType cargoType : CargoType.values()) {
            sum += availableUnloads[cargoType.ordinal()].size();
        }
        return sum;
    }

    public int getNumberWaiting() {
        return nWaiting.get();
    }

    public synchronized Data getCurrentData() {
        return new Data(currentData);
    }
}
