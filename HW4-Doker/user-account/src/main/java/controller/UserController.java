package controller;

import dao.HttpStockDao;
import dao.StockDao;
import dao.UserDao;
import model.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class UserController {
    private final UserDao userDao;
    private final StockDao stockDao;

    public UserController(UserDao userDao) {
        this.userDao = userDao;
        this.stockDao = new HttpStockDao(userDao, "http://localhost:8080");
    }

    @RequestMapping(value = "/add_user", method = RequestMethod.POST)
    public String addUser(
            @RequestParam String name,
            @RequestParam Long balance
    ) {
        User user = new User(-1, name, balance);
        int id = userDao.addUser(user);
        return String.valueOf(id);
    }

    @RequestMapping(value = "/add_balance", method = RequestMethod.POST)
    public String addBalance(
            @RequestParam int userId,
            @RequestParam Long amount
    ) {
        userDao.addBalance(userId, amount);
        return "";
    }

    @RequestMapping(value = "/balance", method = RequestMethod.GET)
    public String getBalance(
            @RequestParam int userId
    ) {
        return userDao.getUser(userId).getBalance().toString();
    }

    @RequestMapping(value = "/buy", method = RequestMethod.POST)
    public String buy(
            @RequestParam int userId,
            @RequestParam int stockId,
            @RequestParam Long amount
    ) throws URISyntaxException, IOException, InterruptedException {
        stockDao.buy(userId, stockId, amount);
        return "";
    }

    @RequestMapping(value = "/user_stocks", method = RequestMethod.GET)
    public String userStocks(
            @RequestParam int userId
    ) {
        Map<Integer, Long> stocks = userDao.getStocks(userId);
        return "{" + stocks.entrySet().stream()
                .map(e -> e.getKey() +
                        ": {amount: " + e.getValue() +
                        ", price: " + stockDao.getPrice(e.getKey()) +
                        "}"
                )
                .collect(Collectors.joining(", ")) + "}";
    }

    @RequestMapping(value = "/user_worth", method = RequestMethod.GET)
    public String userWorth(
            @RequestParam int userId
    ) {
        User user = userDao.getUser(userId);
        Long stocksWorth = stockDao.calcWorth(user.getStocksAmount());
        Long balance = user.getBalance();
        return String.valueOf(stocksWorth + balance);
    }

}
