public class Unloader implements Runnable {
    private final SeaPort port;
    private final CargoType cargoType;
    private Data endOfTask;
    private int lastCheckedMinutes = SeaPort.DEFAULT_CURRENT_DATA;

    public Unloader(SeaPort port, CargoType cargoType) {
        this.port = port;
        this.cargoType = cargoType;
        this.endOfTask = null;
    }

    public CargoType getCargoType() {
        return cargoType;
    }

    @Override
    public void run() {
        port.incrementnUnloadersTotal();
        while (port.isRunning()) {
            synchronized (port) {
                port.incrementnWaiting();
                while (port.isCurrentData(lastCheckedMinutes) && port.isRunning()) {
                    try {
                        port.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!port.isRunning()) break;
                lastCheckedMinutes = port.getCurrentData().toMinutes();
            }
            if ((endOfTask != null) && (lastCheckedMinutes == endOfTask.toMinutes())) {
                endOfTask = null;
            }
            if (endOfTask == null) {
                endOfTask = port.work(this);
            }
        }
        port.decrementnUnloadersTotal();
    }
}
