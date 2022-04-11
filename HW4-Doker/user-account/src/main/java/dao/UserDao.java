package dao;

import model.User;

import java.util.Map;

public interface UserDao {
    int addUser(User user);

    void addBalance(int userId, Long amount);

    Map<Integer, Long> getStocks(int userId);

    void addStocks(int userId, int stockId, Long amount);

    User getUser(int userId);
}
