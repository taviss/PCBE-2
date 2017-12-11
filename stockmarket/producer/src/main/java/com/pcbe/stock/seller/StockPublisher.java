package com.pcbe.stock.seller;

import com.pcbe.stock.event.Offer;
import com.pcbe.stock.event.StockEvent;
import com.pcbe.stock.event.StockEventType;
import com.pcbe.stock.seller.ui.StockPublisherGUI;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StockPublisher implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(StockPublisher.class);
    
    public static String topicName = "PCBE-Stock";

    private String id;
    
    private Connection connection;
    private Session session;
    private Topic destination;

    private MessageProducer messageProducer;
    private MessageConsumer messageConsumer;
    
    private StockPublisherGUI stockPublisherGUI;
    
    private AtomicInteger offers;
    
    public StockPublisher(String id) {
        this.id = id;
        this.offers = new AtomicInteger(0);
    }

    public void dispose() throws JMSException {
        session.close();
        connection.close();
    }
    
    public int getNextId() {
        return this.offers.getAndIncrement();
    }

    @Override
    public void run() {
        this.stockPublisherGUI = new StockPublisherGUI(this.id);
        this.stockPublisherGUI.setListener(this);
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setBrokerURL(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
            connectionFactory.setTrustedPackages(new ArrayList<>(Arrays.asList("com.pcbe.stock", "java.sql", "java.lang", "java.util")));

            connection = connectionFactory.createConnection();
            connection.setClientID(this.id);
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            destination = session.createTopic(topicName);
            
            messageConsumer = session.createDurableSubscriber(destination, "CONN" + this.id, "(eventType='" + StockEventType.ST_OFFER_READ + "' OR eventType='" + StockEventType.ST_OFFER_BID + "') AND senderId='" + this.id + "' OR eventType='" + StockEventType.ST_OFFER_REQUEST + "'", true);
            messageConsumer.setMessageListener(new StockMarketListener());

            messageProducer = session.createProducer(destination);
            messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
        } catch(JMSException e) {
            LOG.error(e.getMessage());
        }
    }

    public void createNewOffer(Offer offer) throws JMSException {
        ObjectMessage objectMessage = session.createObjectMessage();

        StockEvent stockEvent = new StockEvent(StockEventType.ST_NEW_OFFER, offer);
        objectMessage.setObject(stockEvent);
        objectMessage.setStringProperty("senderId", this.id);
        objectMessage.setStringProperty("eventType", StockEventType.ST_NEW_OFFER.toString());
        objectMessage.setFloatProperty("price", offer.getPrice());
        objectMessage.setFloatProperty("oldPrice", offer.getPrice());
        objectMessage.setLongProperty("dateAvailable", offer.getCreationDate());
        objectMessage.setStringProperty("company", offer.getCompany());

        LOG.info(offer.getCompany() + " sent new offer with id" + offer.getId() + ":\n" +
                "Price: " + offer.getPrice() + "\n" +
                "Date: " + offer.getCreationDate() + "\n");

        messageProducer.send(objectMessage);
    }
    
    public void createNewOffer(String title, float price, String description) throws JMSException {
        ObjectMessage objectMessage = session.createObjectMessage();
        
        int offerId = this.offers.getAndIncrement();
        Offer offer = new Offer(
                title,
                this.id,
                price,
                offerId,
                description,
                false
        );

        StockEvent stockEvent = new StockEvent(StockEventType.ST_NEW_OFFER, offer);
        objectMessage.setObject(stockEvent);
        objectMessage.setStringProperty("senderId", this.id);
        objectMessage.setStringProperty("eventType", StockEventType.ST_NEW_OFFER.toString());
        objectMessage.setFloatProperty("price", offer.getPrice());
        objectMessage.setFloatProperty("oldPrice", offer.getPrice());
        objectMessage.setLongProperty("dateAvailable", offer.getCreationDate());
        objectMessage.setStringProperty("company", offer.getCompany());

        LOG.info(offer.getCompany() + " sent new offer with id" + offer.getId() + ":\n" +
                "Price: " + offer.getPrice() + "\n" +
                "Date: " + offer.getCreationDate() + "\n");

        messageProducer.send(objectMessage);
    }

    public void updateOffer(Offer offer) throws JMSException {
        ObjectMessage objectMessage = session.createObjectMessage();
        
        StockEvent stockEvent = new StockEvent(StockEventType.ST_OFFER_CHANGE, offer);
        objectMessage.setObject(stockEvent);
        objectMessage.setStringProperty("senderId", this.id);
        objectMessage.setStringProperty("eventType", StockEventType.ST_OFFER_CHANGE.toString());
        objectMessage.setFloatProperty("price", offer.getPrice());
        objectMessage.setFloatProperty("oldPrice", offer.getOldPrice());
        objectMessage.setLongProperty("dateAvailable", offer.getCreationDate());
        objectMessage.setStringProperty("company", offer.getCompany());

        LOG.info(offer.getCompany() + " sent updated offer with id " + offer.getId() + ":\n" +
                "Price: " + offer.getPrice() + "\n" +
                "Date: " + offer.getCreationDate() + "\n");

        messageProducer.send(objectMessage);
    }

    public void closeOffer(Offer offer) throws JMSException {
        offer.close();

        ObjectMessage objectMessage = session.createObjectMessage();
        StockEvent stockEvent = new StockEvent(StockEventType.ST_OFFER_CLOSED, offer);
        objectMessage.setObject(stockEvent);
        objectMessage.setStringProperty("senderId", this.id);
        objectMessage.setStringProperty("eventType", StockEventType.ST_OFFER_CLOSED.toString());

        LOG.info(offer.getCompany() + " sent close offer with id" + offer.getId() + ":\n" +
                "Price: " + offer.getPrice() + "\n" +
                "Date: " + offer.getCreationDate() + "\n");

        messageProducer.send(objectMessage);
    }

    private final class StockMarketListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            try {
                if(message instanceof ObjectMessage) {
                    Object obj = ((ObjectMessage) message).getObject();
                    if(obj instanceof StockEvent) {
                        LOG.info(id + " received: " + message.getStringProperty("eventType"));
                        StockEvent stockEvent = (StockEvent) obj;
                        stockPublisherGUI.updateOffer(stockEvent.getOffer());
                        LOG.info(id + " update for offer " + stockEvent.getOffer().getCompany() + ":" + stockEvent.getOffer().getId());
                    }
                } else if(message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    if(message.getStringProperty("eventType").equals(StockEventType.ST_OFFER_REQUEST.toString())) {
                        LOG.info("Sending available offers update...");
                        stockPublisherGUI.updateAllOffers();
                    }
                    LOG.info("Producer " + id + " received message: " + textMessage.getText());
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }
}
