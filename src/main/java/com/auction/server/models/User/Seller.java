public class Seller <T extends Item> extends User {
    private String store_name;
    private T[] itemList;
    private double totalScale; //Tong loi nhuan

    public createAuction(T item, double startingPrices, LocalDateTime time) {
        //Tao phien dau gia
    }
    public void updateItemInfo() {
        //update san pham dau gia
    }
    public void cancelAuction() {
        //Huy phien
    }
}