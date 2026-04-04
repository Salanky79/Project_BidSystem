package src.models.item;

import src.models.item.Item;

public class Antique extends Item {
    private String era;                     // Thời kỳ / Niên đại (VD: Thời Lê, Thế kỷ 19)
    private String material;                // Chất liệu (VD: Gốm sứ, Gỗ sưa, Đồng thau)
    private boolean hasAuthenticityCert;    // Có giấy chứng nhận hàng thật (Authentic) không?

    public Antique(String name, String description, double startingPrice, int quantity,
                   String condition, String era,
                   String material, boolean hasAuthenticityCert) {

        // Gọi lại constructor của class cha (Item)
        super(name, description, startingPrice, quantity, condition);

        this.era = era;
        this.material = material;
        this.hasAuthenticityCert = hasAuthenticityCert;
    }

    public String getEra() { return era; }
    public String getMaterial() { return material; }

    public boolean isHasAuthenticityCert() { return hasAuthenticityCert; }
}