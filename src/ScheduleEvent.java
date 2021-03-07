public class ScheduleEvent {
    private Ship ship;
    private Data startingData;
    private Time unloadingTime;

    public ScheduleEvent(Ship ship, Data data) {
        this.startingData = data;
        this.unloadingTime = new Time((int) (ship.getCargoAmount() / ship.getCargoType().getUnloadingSpeed()));
        this.ship = ship;
    }

    public ScheduleEvent(Ship ship, Data startingData, Time unloadingTime) {
        this.startingData = startingData;
        this.unloadingTime = unloadingTime;
        this.ship = ship;
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

    public void setInvolvedShip(Ship ship) {
        this.ship = ship;
    }

    public Data getStartingData() {
        return startingData;
    }

    public void setStartingData(Data data) {
        this.startingData = data;
    }

    public Time getUnloadingTime() {
        return unloadingTime;
    }

    public void setUnloadingTime(Time time) {
        this.unloadingTime = time;
    }

    public Data getEndingData() {
        return new Data(startingData.toMinutes() + unloadingTime.toMinutes());
    }
}
