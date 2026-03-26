public class Vehicle extends Item {
    private double mileage;
    private String fuelType;

    public Vehicle(String name, String description, double startingPrice, int quantity,
                   String condition, long sellerId, double mileage, String fuelType) {
        super(name, description, startingPrice, quantity, condition, sellerId);
        this.mileage = mileage;
        this.fuelType = fuelType;
    }

    public double getMileage() { return mileage; }
    public String getFuelType() { return fuelType; }
}
