public class Ship {
    private final String name;
    private final CargoType cargoType;
    private int cargoAmount;

    public Ship(String name, CargoType cargoType, int cargoAmount) {
        this.name = name;
        this.cargoType = cargoType;
        this.cargoAmount = cargoAmount;
    }

    public Ship(Ship other) {
        this(other.name, other.cargoType, other.cargoAmount);
    }

    @Override
    public String toString() {
        String caption = String.format("Ship: %s", name) + '\n';
        String cargo;
        switch (cargoType) {
            case LOOSE -> cargo = String.format("Loose, %d tons", cargoAmount);
            case LIQUID -> cargo = String.format("Liquid, %d tons", cargoAmount);
            case CONTAINERS -> cargo = String.format("Containers, %d containers", cargoAmount);
            default -> cargo = "ERROR";
        }
        return caption + cargo;
    }

    public String getName() {
        return name;
    }

    public CargoType getCargoType() {
        return cargoType;
    }

    public int getCargoAmount() {
        return cargoAmount;
    }

    public void setCargoAmount(int cargoAmount) {
        this.cargoAmount = cargoAmount;
    }

    public Time getUnloadingTime() {
        return new Time((getCargoAmount() + getCargoType().getAmount() - 1) /
                getCargoType().getAmount() * getCargoType().getPeriod().toMinutes());
    }
}
