public class Item extends Entity {
    private String name;
    private String description; // BỔ SUNG THÊM MÔ TẢ THEO YÊU CẦU
    private double startingPrice;

    public Item(String name, String description, double startingPrice) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
}