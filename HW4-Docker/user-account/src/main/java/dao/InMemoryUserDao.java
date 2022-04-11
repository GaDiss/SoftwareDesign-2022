package dao;

import model.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryUserDao implements UserDao {
    private final List<User> users = new CopyOnWriteArrayList<>();

    public int addUser(User user) {
        int id = users.size();
        user.setId(id);
        users.add(user);
        return id;
    }

    public void addBalance(int userId, Long amount) {
        users.get(userId).addBalance(amount);
    }

    public Map<Integer, Long> getStocks(int userId) {
        return users.get(userId).getStocksAmount();
    }

    public void addStocks(int userId, int stockId, Long amount) {
        users.get(userId).addStock(stockId, amount);
    }

    public User getUser(int userId) {
        return users.get(userId);
    }
}
