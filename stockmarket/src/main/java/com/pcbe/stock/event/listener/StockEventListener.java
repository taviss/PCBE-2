package com.pcbe.stock.event.listener;

import com.pcbe.stock.event.StockEvent;

import java.util.EventListener;

public interface StockEventListener extends EventListener {
    void onEvent(final StockEvent event);
}
