package com.pcbe.stock.buyer;

import com.pcbe.stock.event.Offer;
import com.pcbe.stock.buyer.ui.StockConsumerGUI;
import com.pcbe.stock.event.StockEvent;
import com.pcbe.stock.event.StockEventType;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Arrays;

public class StockConsumer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(StockConsumer.class);
    
    public static String topicName = "PCBE-Stock";
    
    private String id;
    
    private Connection connection;
    private Session session;
    private Topic destination;
    
    private MessageConsumer messageConsumer;
    private MessageProducer messageProducer;
    
    private String filter;
    private StockConsumerGUI stockConsumerGUI;
    
    private float minPrice;
    private float maxPrice;
    private long minDate;
    private String company;
    
    public StockConsumer(String id, float minPrice, float maxPrice, long minDate) {
        this.id = id;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minDate = minDate;
        this.company = "";
    }
    
    public void setMinPrice(float minPrice) {
        this.minPrice = minPrice;
    }
    
    public void setMaxPrice(float maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public void setMinDate(long date) {
        this.minDate = date;
    }
    
    private String buildFilter() {
        return "(" +
                    "(" +
                        "eventType = '" + StockEventType.ST_NEW_OFFER + "'" +
                        " OR " +
                        "eventType='" + StockEventType.ST_OFFER_CHANGE + "'" +
                    ")" + " AND" +
                    " (price BETWEEN " + minPrice + " AND " + maxPrice + " OR" +
                    " oldPrice BETWEEN " + minPrice + " AND " + maxPrice + ")" +
                    " AND" +
                    " dateAvailable >= " + minDate + 
                    ((company == null || company.equals("")) ? "" : " AND" + " company = '" + company + "'") +
                ") OR eventType= '" + StockEventType.ST_OFFER_CLOSED + "'";
                
    }
    
    public void dispose() throws JMSException {
        session.close();
        connection.close();
    }
    
    public void notifyOfferSeen(Offer offer) {
        try {
            offer.increment();
            StockEvent stockEvent = new StockEvent(StockEventType.ST_OFFER_READ, offer);
            ObjectMessage objectMessage = session.createObjectMessage();
            objectMessage.setStringProperty("eventType", StockEventType.ST_OFFER_READ.toString());
            objectMessage.setStringProperty("senderId", offer.getCompany());
            objectMessage.setObject(stockEvent);
            messageProducer.send(objectMessage);
        } catch(JMSException e) {
            LOG.error(e.getMessage());
        }
    }

    public void notifyOfferBid(Offer offer) {
        try {
            offer.increment();
            StockEvent stockEvent = new StockEvent(StockEventType.ST_OFFER_BID, offer);
            ObjectMessage objectMessage = session.createObjectMessage();
            objectMessage.setStringProperty("eventType", StockEventType.ST_OFFER_BID.toString());
            objectMessage.setStringProperty("senderId", offer.getCompany());
            objectMessage.setObject(stockEvent);
            messageProducer.send(objectMessage);
        } catch(JMSException e) {
            LOG.error(e.getMessage());
        }
    }
    
    public void requestOffers() {
        try {
            TextMessage textMessage = session.createTextMessage();
            textMessage.setStringProperty("eventType", StockEventType.ST_OFFER_REQUEST.toString());
            messageProducer.send(textMessage);
        } catch(JMSException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void run() {
        this.stockConsumerGUI = new StockConsumerGUI(this.id, minPrice, maxPrice);
        this.stockConsumerGUI.setListener(this);
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setBrokerURL(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
            connectionFactory.setTrustedPackages(new ArrayList<>(Arrays.asList("com.pcbe.stock", "java.sql", "java.lang", "java.util")));

            connection = connectionFactory.createConnection();
            connection.setClientID(this.id);
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            destination = session.createTopic(topicName);
            
            filter = buildFilter();
            
            messageConsumer = session.createDurableSubscriber(destination, "CONN" + this.id, filter, true);
            messageConsumer.setMessageListener(new StockMarketListener());

            messageProducer = session.createProducer(destination);
            messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
            
            requestOffers();
        } catch(JMSException e) {
            LOG.error(e.getMessage());
        }
    }
    
    public boolean resubscribe() {
        try {
            LOG.info("Building new filter...");
            String newFilter = buildFilter();
            LOG.info(newFilter);
            
            //No need to resubscribe if filter hasn't changed
            if(newFilter.equals(filter)) {
                LOG.info("No resubscription needed.");
                return false;
            }

            messageConsumer.close();
            LOG.info("Message consumer closed...");
            
            filter = newFilter;

            messageConsumer = session.createDurableSubscriber(destination, "CONN" + this.id, filter, true);
            messageConsumer.setMessageListener(new StockMarketListener());
            LOG.info("Message consumer recreated...");
            requestOffers();
            LOG.info("Sent request offers message...");
            return true;
        } catch(JMSException e) {
            LOG.error(e.getMessage());
        }
        return false;
    }

    private final class StockMarketListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            try {
                if(message instanceof ObjectMessage) {
                    LOG.info(id + " received: " + message.getStringProperty("eventType"));
                    Object obj = ((ObjectMessage) message).getObject();
                    
                    if(obj instanceof StockEvent) {
                        StockEvent stockEvent = (StockEvent)obj;
                        stockConsumerGUI.updateOffer(stockEvent.getOffer());
                        LOG.info(id + " update for offer " + stockEvent.getOffer().getCompany() + ":" + stockEvent.getOffer().getId());
                    }
                } else if(message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    LOG.info("Producer " + id + " received message: " + textMessage.getText());
                }
            } catch (JMSException e) {
                LOG.error(e.getMessage());
            }
        }

    }
}
