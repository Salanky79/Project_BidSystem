package models.item;

class RealEstate extends Item {
    private String propertyType;    // Loại BĐS (VD: Đất nền, Căn hộ chung cư, Biệt thự)
    private String location;        // Địa chỉ cụ thể
    private double areaSquareMeter; // Diện tích (m²)
    private String legalStatus;     // Tình trạng pháp lý (VD: Sổ đỏ chính chủ, Hợp đồng mua bán)

    public RealEstate(String name, String description, double startingPrice, int quantity,
                      String condition, String propertyType,
                      String location, double areaSquareMeter, String legalStatus) {

        // Gọi lại constructor của class cha (Item)
        super(name, description, startingPrice, quantity, condition);

        this.propertyType = propertyType;
        this.location = location;
        this.areaSquareMeter = areaSquareMeter;
        this.legalStatus = legalStatus;
    }

    public String getPropertyType() { return propertyType; }
    public String getLocation() { return location; }
    public double getAreaSquareMeter() { return areaSquareMeter; }
    public String getLegalStatus() { return legalStatus; }
}
