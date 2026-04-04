public class Vehicle extends Item {
    private double mileage;
    private String fuelType;

    public Vehicle(String name, String description, double startingPrice, double mileage, String fuelType) {
        super(name, description, startingPrice);
        this.mileage = mileage;
        this.fuelType = fuelType;
    }

    public String getFuelType() {
        return fuelType;
    }
    public double getMileage() {
        return mileage;
    }
}
