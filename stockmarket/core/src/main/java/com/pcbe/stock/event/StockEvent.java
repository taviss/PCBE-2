package com.pcbe.stock.event;

public class StockEvent {
    private StockEventType stockEventType;
    private StockEventData stockEventData;
    
    public StockEvent(final StockEventType stockEventType) {
        this.stockEventType = stockEventType;
    }
    
    public StockEvent(final StockEventType stockEventType, final StockEventData stockEventData) {
        this(stockEventType);
        this.stockEventData = stockEventData;
    }

    public StockEventType getStockEventType() {
        return stockEventType;
    }

    public StockEventData getStockEventData() {
        return stockEventData;
    }
}
