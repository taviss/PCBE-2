package com.pcbe.stock.event;

import java.io.Serializable;

public enum StockEventType implements Serializable {
    ST_NEW_OFFER("ST_NEW_OFFER"),
    ST_OFFER_CHANGE("ST_OFFER_CHANGE"),
    ST_OFFER_CLOSED("ST_OFFER_CLOSED"),
    ST_OFFER_READ("ST_OFFER_READ"),
    ST_OFFER_BID("ST_OFFER_BID"),
    ST_OFFER_REQUEST("ST_OFFER_REQUEST");
    
    private String name;
    
    StockEventType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
