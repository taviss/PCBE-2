package com.pcbe.stock.buyer;

import com.pcbe.stock.event.StockEvent;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class StockConsumer {
    public static String topicName = "PCBE-Stock";
    
    private Connection connection;
    private Session session;
    private Destination destination;
    
    private MessageProducer messageProducer;
    
    public StockConsumer() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);

        connection = connectionFactory.createConnection();
        connection.start();
        
        destination = session.createTopic(topicName);

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(new StockMarketListener());
        
        messageProducer = session.createProducer(destination);

    }
    
    public void dispose() throws JMSException {
        session.close();
        connection.close();
    }

    private final class StockMarketListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            try {
                if(message instanceof ObjectMessage) {
                    Object obj = ((ObjectMessage) message).getObject();
                    
                    if(obj instanceof StockEvent) {
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
