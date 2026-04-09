package com.auction.server.controllers;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.List;
import com.auction.share.models.item.Item;
import com.auction.share.models.auction.BidTransaction;

public class ItemController {

    List<Item> auctionItems = new ArrayList<>();

    // Hàng đợi ưu tiên để xử lý giá thầu (Dành cho chức năng nâng cao)
    PriorityQueue<BidTransaction> bids = new PriorityQueue<>((a, b) ->
            Double.compare(b.getAmount(), a.getAmount()) // Thằng cao tiền nhất đứng đầu [cite: 147]
    );
}

