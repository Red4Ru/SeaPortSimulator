import java.util.ArrayList;
import java.util.List;

public class Unload {
    private final ScheduleEvent originalEvent;
    private final Data startingData;
    private Data currentData;
    private Ship ship;
    private Time extraTime;
    private Time executeTimeTotal;
    private List<Data> endsOfExecutes;

    public Unload(ScheduleEvent originalEvent, Data startingData, Time extraTime) {
        this.originalEvent = new ScheduleEvent(originalEvent);
        this.startingData = new Data(startingData);
        this.currentData = new Data(startingData);
        this.ship = new Ship(originalEvent.getInvolvedShip());
        this.extraTime = new Time(extraTime);
        this.endsOfExecutes = new ArrayList<>();
        this.executeTimeTotal = new Time(0);
    }

    public Unload(Unload unload) {
        this(unload.originalEvent, unload.startingData, unload.extraTime);
        this.currentData = new Data(unload.currentData);
        this.ship = new Ship(unload.ship);
        this.endsOfExecutes = new ArrayList<>(unload.endsOfExecutes);
        this.executeTimeTotal = new Time(unload.executeTimeTotal);
    }

    @Override
    public String toString() {
        return "Unload " +
                "{\nShip name: " + ship.getName() +
                ",\nArriving in the port: " + getStartingData() +
                ",\nTotal waiting time: " + getWaitingTime() +
                ",\nUnloading time: " + getSpentTime() +
                ",\nDeparture from the port: " + getEndingData() +
                ",\nExcess time: " + getExcess() +
                "\n}";
    }

    public void setCurrentData(Data currentData) {
        if (endsOfExecutes.size() > 0) {
            this.executeTimeTotal = new Time(this.executeTimeTotal.toMinutes() +
                    currentData.toMinutes() - this.currentData.toMinutes());
            for (int i = endsOfExecutes.size() - 1; i >= 0; i--) {
                if (endsOfExecutes.get(i).toMinutes() == currentData.toMinutes()) {
                    endsOfExecutes.remove(i);
                }
            }
        }
        this.currentData = new Data(currentData);
    }

    public Data getStartingData() {
        return new Data(startingData);
    }

    public Data getCurrentData() {
        return new Data(currentData);
    }

    public Time getRemainingTime() {
        return new Time(ship.getUnloadingTime().toMinutes() + extraTime.toMinutes());
    }

    public Data getEndingData() {
        return new Data(getCurrentData().toMinutes() + getRemainingTime().toMinutes());
    }

    public Time getExcess() {
        return new Time(Math.max(0, getEndingData().toMinutes() - originalEvent.getEndingData().toMinutes()));
    }

    public Time getSpentTime() {
        return new Time(getCurrentData().toMinutes() - getStartingData().toMinutes());
    }

    public Time getWaitingTime() {
        return new Time(getSpentTime().toMinutes() - executeTimeTotal.toMinutes());
    }

    public CargoType getCargoType() {
        return ship.getCargoType();
    }

    public int getSimultaneousExecutesCount() {
        return endsOfExecutes.size();
    }

    public Data execute() {
        if (extraTime.toMinutes() > 0) {
            extraTime = new Time(Math.max(0, extraTime.toMinutes() - ship.getCargoType().getPeriod().toMinutes()));
        } else {
            ship.setCargoAmount(Math.max(0, ship.getCargoAmount() - ship.getCargoType().getAmount()));
        }
        Data endOfExecute = new Data(currentData.toMinutes() + ship.getCargoType().getPeriod().toMinutes());
        endsOfExecutes.add(endOfExecute);
        return endOfExecute;
    }
}
