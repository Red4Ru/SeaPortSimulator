public enum CargoType {
    LOOSE(new Time(0,2), 150), // amount in tons of loose cargo per unload
    LIQUID(new Time(0,1), 15), // amount in tons of liquid cargo per unload
    CONTAINERS(new Time(0,6), 1); // amount in containers per unload

    private final Time period;
    private final int amount;

    CargoType(Time period, int amount) {
        this.period = period;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public Time getPeriod() {
        return period;
    }
}
