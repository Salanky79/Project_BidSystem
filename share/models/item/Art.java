package models.item;

class Art extends Item {
    private String artist;
    private int year;

    public Art(String name, String description, double startingPrice, int quantity,
               String condition, String artist, int year) {
        super(name, description, startingPrice, quantity, condition);
        this.artist = artist;
        this.year = year;
    }

    public String getArtist() { return artist; }
    public int getYear() { return year; }
}