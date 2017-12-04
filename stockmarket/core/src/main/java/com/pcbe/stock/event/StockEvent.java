package com.pcbe.stock.event;

import java.io.Serializable;

public class StockEvent implements Serializable {
    private StockEventType stockEventType;
    //private StockEventData stockEventData;
    private Offer offer;
    
    public StockEvent(final StockEventType stockEventType) {
        this.stockEventType = stockEventType;
    }
    
    public StockEvent(final StockEventType stockEventType, final Offer offer) {
        this(stockEventType);
        this.offer = offer;
    }

    public StockEventType getStockEventType() {
        return stockEventType;
    }

    public Offer getOffer() {
        return offer;
    }
}
