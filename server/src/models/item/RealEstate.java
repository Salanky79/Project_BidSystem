package src.models.item;

public class RealEstate extends Item {
    private String location;        // Địa chỉ / Vị trí
    private double areaSquareMeter; // Diện tích (m²)
    private int rooms;              // Số phòng

    public RealEstate(String name, String description, double startingPrice, String location, double areaSquareMeter, int rooms) {
        super(name, description, startingPrice);
        this.location = location;
        this.areaSquareMeter = areaSquareMeter;
        this.rooms = rooms;
    }

    public String getLocation() {
        return location;
    }
    public double getAreaSquareMeter() {
        return areaSquareMeter;
    }
    public int getRooms() {
        return rooms;
    }
}
