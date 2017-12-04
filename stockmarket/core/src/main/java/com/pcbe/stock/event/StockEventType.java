package com.pcbe.stock.event;

import java.io.Serializable;

public enum StockEventType implements Serializable {
    ST_NEW_OFFER("ST_NEW_OFFER"),
    ST_OFFER_CHANGE("ST_OFFER_CHANGE"),
    ST_OFFER_CLOSED("ST_OFFER_CLOSED");
    
    private String name;
    
    StockEventType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
