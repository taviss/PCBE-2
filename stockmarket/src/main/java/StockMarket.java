import com.pcbe.stock.buyer.StockConsumer;
import com.pcbe.stock.seller.StockPublisher;


public class StockMarket {
    public static void main(String args[]) {
        new Thread(new StockPublisher("1")).start();
        //new Thread(new StockPublisher("2")).start();
        //new Thread(new StockPublisher("3")).start();
        new Thread(new StockConsumer(300, 900, System.currentTimeMillis())).start();
    }
}
