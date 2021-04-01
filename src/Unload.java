import java.util.ArrayList;
import java.util.List;

public class Unload {
    private final ScheduleEvent originalEvent;
    private final Data startingData;
    private Data currentData;
    private Ship ship;
    private Time extraTime;
    private List<Data> endsOfExecutes;

    public Unload(ScheduleEvent originalEvent, Data startingData, Time extraTime) {
        this.originalEvent = new ScheduleEvent(originalEvent);
        this.startingData = new Data(startingData);
        this.currentData = new Data(startingData);
        this.ship = new Ship(originalEvent.getInvolvedShip());
        this.extraTime = new Time(extraTime);
        this.endsOfExecutes = new ArrayList<>();
    }

    public Unload(Unload unload) {
        this(unload.originalEvent, unload.startingData, unload.extraTime);
        this.currentData = new Data(unload.currentData);
        this.ship = new Ship(unload.ship);
        this.endsOfExecutes = new ArrayList<>(unload.endsOfExecutes);
    }

    @Override
    public String toString() {
        return "Unload{\n" +
                "getOriginalEvent():\n" + getOriginalEvent() +
                ",\ngetStartingData(): " + getStartingData() +
                ",\ngetCurrentData(): " + getCurrentData() +
                ",\ngetRemainingTime(): " + getRemainingTime() +
                ",\ngetEndingData(): " + getEndingData() +
                ",\ngetExcess(): " + getExcess() +
                ",\ngetCargoType(): " + getCargoType() +
                ",\ngetSimultaneousExecutesCount(): " + getSimultaneousExecutesCount() +
                '}';
    }

    public void setCurrentData(Data currentData) {
        this.currentData = currentData;
        for (int i = endsOfExecutes.size() - 1; i >= 0; i--) {
            if (endsOfExecutes.get(i).toMinutes() == currentData.toMinutes()) {
                endsOfExecutes.remove(i);
            }
        }
    }

    public ScheduleEvent getOriginalEvent() {
        return new ScheduleEvent(originalEvent);
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
