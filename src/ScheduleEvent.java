public class ScheduleEvent {
    private final Ship ship;
    private final Data startingData;

    public ScheduleEvent(Ship ship, Data startingData) {
        this.ship = ship;
        this.startingData = startingData;
    }

    @Override
    public String toString() {
        return ship +
                "\nStart: " + getStartingData() +
                "\nUnloading: " + getUnloadingTime() +
                "\nEnd: " + getEndingData();
    }

    public Ship getInvolvedShip() {
        return ship;
    }

    public Data getStartingData() {
        return startingData;
    }

    public Time getUnloadingTime() {
        return ship.getUnloadingTime();
    }

    public Data getEndingData() {
        return new Data(getStartingData().toMinutes() + getUnloadingTime().toMinutes());
    }
}
