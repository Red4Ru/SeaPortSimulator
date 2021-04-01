public class ScheduleEvent {
    private final Ship ship;
    private final Data startingData;

    public ScheduleEvent(Ship ship, Data startingData) {
        this.ship = new Ship(ship);
        this.startingData = new Data(startingData);
    }

    public ScheduleEvent(ScheduleEvent event) {
        this(event.ship, event.startingData);
    }

    @Override
    public String toString() {
        return ship +
                "\nStart: " + getStartingData() +
                "\nUnloading: " + getUnloadingTime() +
                "\nEnd: " + getEndingData();
    }

    public Ship getInvolvedShip() {
        return new Ship(ship);
    }

    public Data getStartingData() {
        return new Data(startingData);
    }

    public Time getUnloadingTime() {
        return new Time(ship.getUnloadingTime());
    }

    public Data getEndingData() {
        return new Data(getStartingData().toMinutes() + getUnloadingTime().toMinutes());
    }
}
