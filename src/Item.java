public class Item extends Entity {
    private String name;
    private String description;
    private double startingPrice;
    private int quantity;
    private String condition;
    private long sellerId;

    public Item(String name, String description, double startingPrice, int quantity, 
                String condition, long sellerId) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.quantity = quantity;
        this.condition = condition;
        this.sellerId = sellerId;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public int getQuantity() { return quantity; }
    public String getCondition() { return condition; }
    public long getSellerId() { return sellerId; }
    }
}

class Art extends Item {
    private String artist;
    private int year;

    public Art(String name, String description, double startingPrice, int quantity, 
               String condition, long sellerId, String artist, int year) {
        super(name, description, startingPrice, quantity, condition, sellerId);
        this.artist = artist;
        this.year = year;
    }

    public String getArtist() { return artist; }
    public int getYear() { return year; }
}

class Electronic extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronic(String name, String description, double startingPrice, int quantity,
                      String condition, long sellerId, String brand, int warrantyMonths) {
        super(name, description, startingPrice, quantity, condition, sellerId);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public String getBrand() { return brand; }
    public String getWarrantyMonths() { return warrantyMonths; }
}

class Vehicle extends Item {
    private double mileage;
    private String fuelType; // Xăng, Diesel, Điện, v.v.

    public Vehicle(String name, String description, double startingPrice, int quantity,
                   String condition, long sellerId, double mileage, String fuelType) {
        super(name, description, startingPrice, quantity, condition, sellerId);
        this.mileage = mileage;
        this.fuelType = fuelType;
    }

    public String getMileage() { return mileage; }
    public String getFuelType() { return fuelType; }
}

class RealEstate extends Item {
    private String location;        // Địa chỉ / Vị trí
    private double areaSquareMeter; // Diện tích (m²)
    private int rooms;              // Số phòng

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

class Jewelry extends Item {
    private String material;        // Vàng, bạc, đồng, v.v.
    private double caratWeight;     // Trọng lượng carat
    private String gemstoneType;    // Loại đá quý (Kim cương, Hồng ngọc, Sapphire, v.v.)

    public Jewelry(String name, String description, double startingPrice, int quantity,
                   String condition,  long sellerId, String material,
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