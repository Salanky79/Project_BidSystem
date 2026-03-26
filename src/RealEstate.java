public class RealEstate extends Item {
    private String location;
    private double areaSquareMeter;
    private int rooms;

    public RealEstate(String name, String description, double startingPrice, int quantity,
                      String condition, long sellerId, String location,
                      double areaSquareMeter, int rooms) {
        super(name, description, startingPrice, quantity, condition, sellerId);
        this.location = location;
        this.areaSquareMeter = areaSquareMeter;
        this.rooms = rooms;
    }

    public String getLocation() { return location; }
    public double getAreaSquareMeter() { return areaSquareMeter; }
    public int getRooms() { return rooms; }
}
