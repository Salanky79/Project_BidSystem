public class Electronic extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronic(String name, String description, double startingPrice, int quantity,
                      String condition, long sellerId, String brand, int warrantyMonths) {
        super(name, description, startingPrice, quantity, condition, sellerId);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public String getBrand() { return brand; }
    public int getWarrantyMonths() { return warrantyMonths; }
}
