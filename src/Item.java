public class Item extends Entity {
    private String name;
    private String description;
    private double startingPrice;

    public Item(String name, String description, double startingPrice) {
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
}

class Art extends Item {
    private String artist;
    private int year;

    public Art(String name, double startingPrice, String artist, int year) {
        super(name, startingPrice);
        this.artist = artist;
        this.year = year;
    }

    public String getArtist() { return artist; }
    public int getYear() { return year; }
}

class Electronic extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronic(String name, double startingPrice, String brand, int warrantyMonths) {
        super(name, startingPrice);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public String getBrand() { return brand; }
    public int getWarrantyMonths() { return warrantyMonths; }
}

class Vehicle extends Item {
    private double mileage;
    private String fuelType;

    public Vehicle(String name, double startingPrice, double mileage, String fuelType) {
        super(name, startingPrice);
        this.mileage = mileage;
        this.fuelType = fuelType;
    }

    public String getFuelType() { return fuelType; }
    public double getMileage() { return mileage; }
}

class RealEstate extends Item {
    private String location;        // Địa chỉ / Vị trí
    private double areaSquareMeter; // Diện tích (m²)
    private int rooms;              // Số phòng

    public RealEstate(String name,  double startingPrice, String location, double areaSquareMeter, int rooms) {
        super(name, startingPrice);
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

    public Jewelry(String name, double startingPrice, String material, double caratWeight, String gemstoneType) {
        super(name, startingPrice);
        this.material = material;
        this.caratWeight = caratWeight;
        this.gemstoneType = gemstoneType;
    }

    public String getMaterial() { return material; }
    public double getCaratWeight() { return caratWeight; }
    public String getGemstoneType() { return gemstoneType; }
}