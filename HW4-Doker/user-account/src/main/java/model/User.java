package model;

import java.util.*;

public class User {
    private int id;
    private final String name;
    private Long balance;
    private final HashMap<Integer, Long> stocksAmount;

    public User(int id, String name, Long balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        stocksAmount = new HashMap<>();
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getBalance() {
        return balance;
    }

    public void addBalance(Long balance) {
        if (this.balance + balance < 0) throw new AssertionError("Negative balance");
        this.balance += balance;
    }

    public Map<Integer, Long> getStocksAmount() {
        return stocksAmount;
    }

    public void addStock(int stockId, Long amount) {
        if (amount > 0) {
            stocksAmount.putIfAbsent(stockId, 0L);
        } else {
            if (!stocksAmount.containsKey(stockId) || stocksAmount.get(stockId) < amount) {
                throw new AssertionError("Not enough stock");
            }
        }

        stocksAmount.computeIfPresent(stockId, (k, v) -> v + amount);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }


}
