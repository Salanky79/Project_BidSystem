package src.models.item;

public class Jewelry extends Item {
    private String material;        // Vàng, bạc, đồng, v.v.
    private double caratWeight;     // Trọng lượng carat
    private String gemstoneType;    // Loại đá quý (Kim cương, Hồng ngọc, Sapphire, v.v.)

    public Jewelry(String name, String description, double startingPrice, String material, double caratWeight, String gemstoneType) {
        super(name, description, startingPrice);
        this.material = material;
        this.caratWeight = caratWeight;
        this.gemstoneType = gemstoneType;
    }

    public String getMaterial() {
        return material;
    }
    public double getCaratWeight() {
        return caratWeight;
    }
    public String getGemstoneType() {
        return gemstoneType;
    }
}
