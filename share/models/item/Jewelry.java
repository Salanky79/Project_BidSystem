package models.item;

import Enum.Category;

class Jewelry extends Item {
    private String material;        // Vàng, bạc, đồng, v.v.
    private double caratWeight;     // Trọng lượng carat
    private String gemstoneType;    // Loại đá quý (Kim cương, Hồng ngọc, Sapphire, v.v.)

    public Jewelry(String name, String description, double startingPrice, int quantity,
                   String condition, String material,
                   double caratWeight, String gemstoneType) {
        super(name, description, startingPrice, quantity, condition);
        this.material = material;
        this.caratWeight = caratWeight;
        this.gemstoneType = gemstoneType;
        this.setCategory(Category.JEWELRY);
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
