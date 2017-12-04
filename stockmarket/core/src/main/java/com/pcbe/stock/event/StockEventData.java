package com.pcbe.stock.event;

import java.io.Serializable;

public class StockEventData implements Serializable {
    private String offerId;
    private Float price;
    private long dateAvailable;
    private String buyKey;
    
    public StockEventData(String offerId, Float price, long dateAvailable, String buyKey) {
        this.offerId = offerId;
        this.price = price;
        this.dateAvailable = dateAvailable;
        this.buyKey = buyKey;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public long getDateAvailable() {
        return dateAvailable;
    }

    public void setDateAvailable(long dateAvailable) {
        this.dateAvailable = dateAvailable;
    }

    public String getBuyKey() {
        return buyKey;
    }

    public void setBuyKey(String buyKey) {
        this.buyKey = buyKey;
    }
}
