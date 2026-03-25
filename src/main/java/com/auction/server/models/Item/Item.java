public abstract class Item extends Entity {
    private String name;
    private String description;
    private double startingPrice;

    public abstract String getDetails();
}