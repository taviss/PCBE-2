package com.pcbe.stock.buyer;

import com.pcbe.stock.event.Offer;
import com.pcbe.stock.buyer.ui.StockConsumerGUI;
import com.pcbe.stock.event.StockEvent;
import com.pcbe.stock.event.StockEventType;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Arrays;

public class StockConsumer implements Runnable {
    public static String topicName = "PCBE-Stock";
    
    private Connection connection;
    private Session session;
    private Destination destination;
    
    private MessageProducer messageProducer;
    
    private String filter;
    private StockConsumerGUI stockConsumerGUI;
    
    private long minPrice;
    private long maxPrice;
    private long minDate;
    
    public StockConsumer(long minPrice, long maxPrice, long minDate) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minDate = minDate;
    }
    
    private String buildFilter() {
        return "(" +
                    "(" +
                        "eventType = '" + StockEventType.ST_NEW_OFFER + "'" +
                        " OR " +
                        "eventType='" + StockEventType.ST_OFFER_CHANGE + "'" +
                    ")" + " AND" +
                    " price BETWEEN " + minPrice + " AND " + maxPrice + " AND" +
                    " dateAvailable >= " + minDate +
                ") OR eventType= '" + StockEventType.ST_OFFER_CLOSED + "'";
                
    }
    
    public void dispose() throws JMSException {
        session.close();
        connection.close();
    }
    
    public void notifyOfferSeen(Offer offer) {
        try {
            ObjectMessage objectMessage = session.createObjectMessage();
            objectMessage.setStringProperty("eventType", "offerRead");
            objectMessage.setStringProperty("sender", offer.getCompany());
            objectMessage.setObject(offer);
            messageProducer.send(objectMessage);
        } catch(JMSException e) {
            //TODO
        }
    }

    @Override
    public void run() {
        this.stockConsumerGUI = new StockConsumerGUI("someTest");
        this.stockConsumerGUI.setListener(this);
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setBrokerURL(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
            connectionFactory.setTrustedPackages(new ArrayList<>(Arrays.asList("com.pcbe.stock", "java.sql", "java.lang", "java.util")));

            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            destination = session.createTopic(topicName);
            
            filter = buildFilter();
            
            MessageConsumer consumer = session.createConsumer(destination, filter);
            consumer.setMessageListener(new StockMarketListener());

            messageProducer = session.createProducer(destination);
        } catch(JMSException e) {
            System.out.println(e.getMessage());
        }
    }

    private final class StockMarketListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            try {
                if(message instanceof ObjectMessage) {
                    System.out.println(Thread.currentThread().getName() + " received " + message.getStringProperty("eventType"));
                    Object obj = ((ObjectMessage) message).getObject();
                    
                    if(obj instanceof StockEvent) {
                        StockEvent stockEvent = (StockEvent)obj; 
                        System.out.println(((StockEvent)obj).getOffer().getId());
                        System.out.println(((StockEvent)obj).getStockEventType().getName());
                        //TODO
                        /*
                        Offer offer = new Offer(
                                stockEvent.getStockEventType().toString(), 
                                message.getStringProperty("senderId"), 
                                stockEvent.getOffer().getPrice(), 
                                stockEvent.getOffer().getId(), 
                                "Random test",
                                stockEvent.getStockEventType().equals(StockEventType.ST_OFFER_CLOSED)
                        );*/
                        stockConsumerGUI.updateOffer(stockEvent.getOffer());
                    }
                } else if(message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    System.out.println("Producer " + Thread.currentThread().getName() + " received message: " + textMessage.getText());
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }
}
