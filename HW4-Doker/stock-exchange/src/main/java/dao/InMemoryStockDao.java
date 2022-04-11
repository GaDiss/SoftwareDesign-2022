package dao;

import model.Stock;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryStockDao implements StockDao {
    private final List<Stock> stocks = new CopyOnWriteArrayList<>();

    @Override
    public int addStock(Stock stock) {
        int id = stocks.size();
        stock.setId(id);
        stocks.add(stock);
        return id;
    }

    public void addAmount(int stockId, Long amount) {
        stocks.get(stockId).addAmount(amount);
    }

    public void changePrice(int stockId, Long price) {
        stocks.get(stockId).setPrice(price);
    }

    public Stock getStock(int stockId) {
        return stocks.get(stockId);
    }
}
