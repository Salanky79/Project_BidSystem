public class Jewelry extends Item {
    private String material;
    private double caratWeight;
    private String gemstoneType;

    public Jewelry(String name, String description, double startingPrice, int quantity,
                   String condition, long sellerId, String material,
                   double caratWeight, String gemstoneType) {
        super(name, description, startingPrice, quantity, condition, sellerId);
        this.material = material;
        this.caratWeight = caratWeight;
        this.gemstoneType = gemstoneType;
    }

    public String getMaterial() { return material; }
    public double getCaratWeight() { return caratWeight; }
    public String getGemstoneType() { return gemstoneType; }
}
