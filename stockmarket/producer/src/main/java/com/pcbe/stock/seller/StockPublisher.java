package com.pcbe.stock.seller;

import com.pcbe.stock.event.Offer;
import com.pcbe.stock.event.StockEvent;
import com.pcbe.stock.event.StockEventType;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StockPublisher implements Runnable {
    public static String topicName = "PCBE-Stock";

    private String id;
    
    private Connection connection;
    private Session session;
    private Destination destination;

    private MessageProducer messageProducer;
    private MessageConsumer messageConsumer;
    
    private AtomicInteger offers;
    private HashMap<Integer, Offer> availableOffers;
    
    public StockPublisher(String id) {
        this.id = id;
        this.offers = new AtomicInteger(0);
        this.availableOffers = new HashMap<>();
    }

    public void dispose() throws JMSException {
        session.close();
        connection.close();
    }

    @Override
    public void run() {
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setBrokerURL(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
            connectionFactory.setTrustedPackages(new ArrayList<>(Arrays.asList("com.pcbe.stock", "java.sql", "java.lang", "java.util")));

            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            destination = session.createTopic(topicName);
            
            messageConsumer = session.createConsumer(destination, "eventType='offerRead' AND senderId='" + this.id + "'");
            messageConsumer.setMessageListener(new StockMarketListener());

            messageProducer = session.createProducer(destination);
        } catch(JMSException e) {
            System.out.println(e.getMessage());
        }
        
        try {
            Thread.sleep(new Random().nextInt(2) * 1000);
            createNewOffer();
            createNewOffer();
            Thread.sleep(new Random().nextInt(2) * 1000);
            createNewOffer();
            Thread.sleep(new Random().nextInt(5) * 1000);
            updateOffer(availableOffers.get(2));
            Thread.sleep(new Random().nextInt(5) * 1000);
            closeOffer(availableOffers.get(1));

            
        } catch (InterruptedException|JMSException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private void createNewOffer() throws JMSException {
        ObjectMessage objectMessage = session.createObjectMessage();
        //StockEventData stockEventData = new StockEventData("test", new Random().nextFloat()*1000, System.currentTimeMillis(), "todoKey");
        int offerId = this.offers.getAndIncrement();
        Offer offer = new Offer(
                "Random title",
                this.id,
                500,
                offerId,
                "Random test",
                false
        );
        
        availableOffers.put(offerId, offer);

        StockEvent stockEvent = new StockEvent(StockEventType.ST_NEW_OFFER, offer);
        objectMessage.setObject(stockEvent);
        objectMessage.setStringProperty("senderId", this.id);
        objectMessage.setStringProperty("eventType", StockEventType.ST_NEW_OFFER.toString());
        objectMessage.setFloatProperty("price", offer.getPrice());
        objectMessage.setLongProperty("dateAvailable", offer.getCreationDate());

        System.out.println("Sent new offer:\n" +
                "Price: " + offer.getPrice() + "\n" +
                "Date: " + offer.getCreationDate());

        messageProducer.send(objectMessage);
    }
    
    private void updateOffer(Offer offer) throws JMSException {
        offer.setPrice(235);
        
        ObjectMessage objectMessage = session.createObjectMessage();
        StockEvent stockEvent = new StockEvent(StockEventType.ST_OFFER_CHANGE, offer);
        objectMessage.setObject(stockEvent);
        objectMessage.setStringProperty("senderId", this.id);
        objectMessage.setStringProperty("eventType", StockEventType.ST_OFFER_CHANGE.toString());
        objectMessage.setFloatProperty("price", offer.getPrice());
        objectMessage.setLongProperty("dateAvailable", offer.getCreationDate());

        System.out.println("Sent update offer:\n" +
                "Price: " + offer.getPrice() + "\n" +
                "Date: " + offer.getCreationDate());

        messageProducer.send(objectMessage);
    }
    
    private void closeOffer(Offer offer) throws JMSException {
        offer.close();
        availableOffers.remove(offer.getId());

        ObjectMessage objectMessage = session.createObjectMessage();
        StockEvent stockEvent = new StockEvent(StockEventType.ST_OFFER_CLOSED, offer);
        objectMessage.setObject(stockEvent);
        objectMessage.setStringProperty("senderId", this.id);
        objectMessage.setStringProperty("eventType", StockEventType.ST_OFFER_CLOSED.toString());

        System.out.println("Sent close offer:\n" +
                "Price: " + offer.getPrice() + "\n" +
                "Date: " + offer.getCreationDate());

        messageProducer.send(objectMessage);
    }

    private final class StockMarketListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            try {
                if(message instanceof ObjectMessage) {
                    Object obj = ((ObjectMessage) message).getObject();

                    if(obj instanceof StockEvent) {
                        System.out.println(((StockEvent)obj).getOffer());
                        System.out.println(((StockEvent)obj).getStockEventType().getName());
                        //TODO
                        //messageProducer.se
                    }
                } else if(message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    System.out.println("Consumer " + Thread.currentThread().getName() + " received message: " + textMessage.getText());
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }
}
