package com.pcbe.stock.event;

public enum StockEventType {
    ST_NEW_OFFER("ST_NEW_OFFER"),
    ST_OFFER_CHANGE("ST_OFFER_CHANGE");
    
    private String name;
    
    StockEventType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
