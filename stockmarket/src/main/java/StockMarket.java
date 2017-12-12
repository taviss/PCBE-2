import com.pcbe.stock.buyer.StockConsumer;
import com.pcbe.stock.seller.StockPublisher;


public class StockMarket {
    public static void main(String args[]) {
        new Thread(new StockConsumer("Consumer1",200, 900, System.currentTimeMillis()-100000), "Consumer 1").start();
        new Thread(new StockConsumer("Consumer2",200, 900, System.currentTimeMillis()-100000), "Consumer 2").start();
        new Thread(new StockConsumer("Consumer3",200, 900, System.currentTimeMillis()-100000), "Consumer 3").start();
        new Thread(new StockPublisher("Company1"), "Producer 1").start();
        //new Thread(new StockPublisher("2")).start();
        //new Thread(new StockPublisher("3")).start();

    }
}
