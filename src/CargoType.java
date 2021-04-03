public enum CargoType {
    LOOSE(new Time(/*period*/2), /*amount in tons of loose cargo per time*/150),
    LIQUID(new Time(/*period*/1), /*amount in tons of liquid cargo per time*/15),
    CONTAINERS(new Time(/*period*/6), /*amount in containers per unload*/1);

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
        return new Time(period);
    }
}
