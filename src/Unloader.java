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
    private static Data currentData = new Data(0);
    private static Time totalDelay = new Time(0);
    private final CargoType cargoType;
    private final Data endOfSimulation;

    public Unloader(CargoType cargoType, Data endOfSimulation) {
        this.cargoType = cargoType;
        this.endOfSimulation = endOfSimulation;
    }

    public static synchronized void addAvailableUnload(Unload unload) {
        Collection<Unload> availableUnloadsSynch = Collections.synchronizedCollection(availableUnloads);
        synchronized (availableUnloadsSynch) {
            System.out.println("before+: " + availableUnloadsSynch.size());
            availableUnloadsSynch.add(unload);
            System.out.println("+1:  now " + availableUnloadsSynch.size());
        }
    }

    public static synchronized void printAvailableUnload() {
        int i = 0;
        Collection<Unload> availableUnloadsSynch = Collections.synchronizedCollection(availableUnloads);
        synchronized (availableUnloadsSynch) {
            for (Unload unloadsSynch : availableUnloadsSynch) {
                i++;
                System.out.printf("%d) %s%n", i, unloadsSynch.toString());
            }
        }
        System.out.println();
    }

    public static synchronized void setCurrentData(Data currentData, long delay) {
        Unloader.currentData = currentData;
        Collection<Unload> availableUnloadsSynch = Collections.synchronizedCollection(availableUnloads);
        synchronized (availableUnloadsSynch) {
            for (Unload unload : availableUnloadsSynch) {
                unload.setCurrentData(Unloader.currentData);
            }
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        setCurrentData(new Data(0), 0);
        totalDelay = new Time(0);
        Collection<Unload> availableUnloadsSynch = Collections.synchronizedCollection(availableUnloads);
        synchronized (availableUnloadsSynch) {
            availableUnloadsSynch.clear();
        }
        nWaiting.set(0);
    }

    public static synchronized int getAvailableUnloadsLen() {
        Collection<Unload> availableUnloadsSynch = Collections.synchronizedCollection(availableUnloads);
        synchronized (availableUnloadsSynch) {
            return availableUnloadsSynch.size();
        }
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
        while ((currentData.toMinutes() < endOfSimulation.toMinutes()) && running.get()) {
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
                    Collection<Unload> availableUnloadsSynch = Collections.synchronizedCollection(availableUnloads);
                    synchronized (availableUnloadsSynch) {
                        PriorityQueue<Unload> aus = new PriorityQueue<>(new SortByEndingTime());
                        aus.addAll(availableUnloadsSynch);
                        availableUnloadsSynch.clear();
                        while (!aus.isEmpty()) {
                            Unload unload = aus.poll();
                            int involvedUnloaders = unload.getSimultaneousExecutesCount();
                            if ((unload.getCargoType() == cargoType) && (involvedUnloaders < MAX_SAME_TIME)) {
                                endOfTask = unload.execute();
                                if (unload.getRemainingTime().toMinutes() == 0) {
                                    totalDelay = new Time((unload.getExcess().getHours() + totalDelay.getHours()) * 60);
                                    System.out.println(totalDelay);
                                    System.out.println("-1");
                                    nUnloadsTotal.incrementAndGet();
                                    break;
                                }
                            }
                            availableUnloadsSynch.add(unload);
                        }
                        availableUnloadsSynch.addAll(aus);
                    }
                }
//                System.out.printf("Exit thread %s\n", Thread.currentThread());
            }
        }
    }
}
