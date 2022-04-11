package dao;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public interface StockDao {
    Long calcWorth(Map<Integer, Long> stocksAmount);

    Long getPrice(int stockId);

    void buy(int userId, int stockId, Long amount) throws URISyntaxException, IOException, InterruptedException;
}
