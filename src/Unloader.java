import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class SortByEndingTime implements Comparator<Unload> {
    @Override
    public int compare(Unload o1, Unload o2) {
        return o1.getEndingData().toMinutes() - o2.getEndingData().toMinutes();
    }
}

public class Unloader implements Runnable {
    public static final int MAX_SAME_TIME = 2;
    private static final PriorityQueue<Unload>[] availableUnloads = new PriorityQueue[CargoType.values().length];
    private static final Object[] mutexes = new Object[CargoType.values().length];
    private static final Object totalDelayMutex = new Object();
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static final AtomicInteger nWaiting = new AtomicInteger(0);
    private static final AtomicInteger nUnloadsTotal = new AtomicInteger(0);
    private static Data currentData = new Data(-1);
    private static Time totalDelay = new Time(0);
    private final CargoType cargoType;

    public Unloader(CargoType cargoType) {
        this.cargoType = cargoType;
    }

    public static synchronized void addAvailableUnload(Unload unload) {
//            System.out.println("before+: " + availableUnloadsSynch.size());
        availableUnloads[unload.getCargoType().ordinal()].add(unload);
//            System.out.println("+1:  now " + availableUnloadsSynch.size());
    }

    public static synchronized void printAvailableUnload() {
        int i = 0;
        for (CargoType cargoType : CargoType.values()) {
            System.out.printf("%s:\n", cargoType);
            for (Unload unload : availableUnloads[cargoType.ordinal()]) {
                i++;
                System.out.printf("%d) %s%n", i, unload.toString());
            }
        }
        System.out.println();
    }

    public static synchronized void setCurrentData(Data currentData) {
        nWaiting.set(0);
        Unloader.currentData = currentData;
        for (CargoType cargoType : CargoType.values()) {
            for (Unload unload : availableUnloads[cargoType.ordinal()]) {
                unload.setCurrentData(Unloader.currentData);
            }
        }
        Unloader.class.notifyAll();
    }

    public static synchronized int getPenalty(int pennyPerHour) {
        synchronized (totalDelayMutex) {
            return pennyPerHour * totalDelay.getHours();
        }
    }

    public static synchronized void reset(long delay) {
        running.set(false);
        Unloader.class.notifyAll();
        try {
            Unloader.class.wait(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        running.set(true);
        currentData = new Data(-1);
        synchronized (totalDelayMutex) {
            totalDelay = new Time(0);
        }
        for (CargoType cargoType : CargoType.values()) {
            availableUnloads[cargoType.ordinal()] = new PriorityQueue<>(new SortByEndingTime());
            mutexes[cargoType.ordinal()] = new Object();
        }
        nWaiting.set(0);
    }

    public static int getAvailableUnloadsLen() {
        int sum = 0;
        for (CargoType cargoType : CargoType.values()) {
            sum += availableUnloads[cargoType.ordinal()].size();
        }
        return sum;
    }

    public static int getNumberWaiting() {
        return nWaiting.get();
    }

    public static int getNumberUnloads() {
        return nUnloadsTotal.get();
    }

    @Override
    public void run() {
        Data endOfTask = null;
        int lastCheckedMinutes = -1;
        PriorityQueue<Unload> checked = new PriorityQueue<>(new SortByEndingTime());
        while (running.get()) {
            synchronized (Unloader.class) {
                nWaiting.incrementAndGet();
                while ((lastCheckedMinutes == currentData.toMinutes()) && running.get()) {
                    try {
                        Unloader.class.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!running.get()) break;
                lastCheckedMinutes = currentData.toMinutes();
            }
            if ((endOfTask != null) && (lastCheckedMinutes == endOfTask.toMinutes())) {
                endOfTask = null;
            }
            if (endOfTask == null) {
                synchronized (mutexes[cargoType.ordinal()]) {
                    while (!availableUnloads[cargoType.ordinal()].isEmpty()) {
                        Unload unload = availableUnloads[cargoType.ordinal()].poll();
                        int involvedUnloaders = unload.getSimultaneousExecutesCount();
                        if (involvedUnloaders < MAX_SAME_TIME) {
                            endOfTask = unload.execute();
                            if (unload.getRemainingTime().toMinutes() == 0) {
                                synchronized (totalDelayMutex) {
                                    totalDelay = new Time((unload.getExcess().getHours() + totalDelay.getHours()) * 60);
                                }
                                nUnloadsTotal.incrementAndGet();
                            } else {
                                checked.add(unload);
                            }
                            break;
                        }
                        checked.add(unload);
                    }
                    availableUnloads[cargoType.ordinal()].addAll(checked);
                }
                checked.clear();
            }
        }
    }
}
