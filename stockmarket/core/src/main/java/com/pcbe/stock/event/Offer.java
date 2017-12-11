package com.pcbe.stock.event;

import java.io.Serializable;
import java.util.Date;

public class Offer implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean closed = false;
    private int views = 0;
    private boolean visibleViews = false;
    private String title;
    private String description;
    private Float price;
    private Float oldPrice;
    private long creationDate;
    private Date lastChanged;
    private String company;
    private int id;
    private String buyer = "";

    public Offer(String title, String company, float price, int id, String description, boolean closed) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.oldPrice = 0.0f;
        this.creationDate = System.currentTimeMillis();
        this.lastChanged = new Date();
        this.company = company;
        this.id = id;
        this.closed = closed;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed=true;
    }

    public int getId() {
        return id;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.oldPrice = this.price;
        this.price = price;
    }
    
    public float getOldPrice() {
        return this.oldPrice;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public Date getLastChanged() {
        return lastChanged;
    }

    public String getCompany() {
        return company;
    }

    public void increment() {
        this.views = this.views + 1;
    }

    public int getViews() {
        return this.views;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public void setTitle(String text) {
        title = text;
    }

    public void setDescription(String text) {
        description = text;
    }

    public void setLastChanged() {
        lastChanged = new Date();
    }

    public boolean areViewsVisible() {
        return visibleViews;
    }

    public void setVisibleViews(boolean flag) {
        visibleViews = flag;
    }

    public void setHighestBidder(String username) {
        buyer = username;
    }

    public String getHighestBidder() {
        return buyer;
    }
    
    public boolean equals(Object object) {
        if(object instanceof Offer) {
            Offer otherOffer = (Offer) object;
            if(this.id == otherOffer.id &&
                    this.company.equals(otherOffer.company) &&
                    this.price == otherOffer.price &&
                    this.oldPrice == otherOffer.oldPrice) {
                return true;
            }
        }
        return false;
    }

}