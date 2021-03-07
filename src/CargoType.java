public enum CargoType {
    LOOSE(75.0), // ton of loose cargo per minute
    LIQUID(15.0), // ton of liquid cargo per minute
    CONTAINERS(1.0 / 6); // containers per hour

    private final double unloadingSpeed;

    CargoType(double unloadingSpeed) {
        this.unloadingSpeed = unloadingSpeed;
    }

    public double getUnloadingSpeed() {
        return unloadingSpeed;
    }
}
