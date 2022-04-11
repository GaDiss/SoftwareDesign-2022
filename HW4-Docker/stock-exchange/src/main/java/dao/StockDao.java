package dao;

import model.Stock;

public interface StockDao {
    int addStock(Stock stock);

    void addAmount(int stockId, Long amount);

    void changePrice(int stockId, Long price);

    Stock getStock(int stockId);
}
