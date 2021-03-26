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
    private static final PriorityQueue<Unload> availableUnloads = new PriorityQueue<>(new SortByEndingTime());
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
        availableUnloads.add(unload);
//            System.out.println("+1:  now " + availableUnloadsSynch.size());
    }

    public static synchronized void printAvailableUnload() {
        int i = 0;
        for (Unload unload : availableUnloads) {
            i++;
            System.out.printf("%d) %s%n", i, unload.toString());
        }
        System.out.println();
    }

    public static synchronized void setCurrentData(Data currentData) {
        nWaiting.set(0);
        Unloader.currentData = currentData;
        for (Unload unload : availableUnloads) {
            unload.setCurrentData(Unloader.currentData);
        }
        Unloader.class.notifyAll();
    }

    public static synchronized int getPenalty(int pennyPerHour) {
        return pennyPerHour * totalDelay.getHours();
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
        totalDelay = new Time(0);
        availableUnloads.clear();
        nWaiting.set(0);
    }

    public static int getAvailableUnloadsLen() {
        return availableUnloads.size();
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
//                System.out.printf("Enter thread %s\n", Thread.currentThread());
                lastCheckedMinutes = currentData.toMinutes();
                if ((endOfTask != null) && (currentData.toMinutes() == endOfTask.toMinutes())) {
                    endOfTask = null;
                }
                if (endOfTask == null) {
                    while (!availableUnloads.isEmpty()) {
                        Unload unload = availableUnloads.poll();
                        int involvedUnloaders = unload.getSimultaneousExecutesCount();
                        if ((unload.getCargoType() == cargoType) && (involvedUnloaders < MAX_SAME_TIME)) {
                            endOfTask = unload.execute();
                            if (unload.getRemainingTime().toMinutes() == 0) {
                                totalDelay = new Time((unload.getExcess().getHours() + totalDelay.getHours()) * 60);
//                                System.out.println(totalDelay);
//                                    System.out.println("-1");
                                nUnloadsTotal.incrementAndGet();
                                break;
                            }
                        }
                        checked.add(unload);
                    }
                    availableUnloads.addAll(checked);
                    checked.clear();
                }
//                System.out.printf("Exit thread %s\n", Thread.currentThread());
            }
        }
    }
}
