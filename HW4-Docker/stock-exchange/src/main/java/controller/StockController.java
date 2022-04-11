package controller;

import dao.StockDao;
import model.Stock;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class StockController {
    private final StockDao stockDao;

    public StockController(StockDao stockDao) {
        this.stockDao = stockDao;
    }

    @RequestMapping(value = "/add_company", method = RequestMethod.POST)
    public String addCompany(
            @RequestParam String companyName,
            @RequestParam Long price,
            @RequestParam Long amount
    ) {
        Stock stock = new Stock(-1, companyName, price, amount);

        int id = stockDao.addStock(stock);

        return String.valueOf(id);
    }

    @RequestMapping(value = "/add_amount", method = RequestMethod.POST)
    public String addStocks(
            @RequestParam int stockId,
            @RequestParam Long amount
    ) {
        stockDao.addAmount(stockId, amount);
        return "";
    }

    @RequestMapping(value = "/buy", method = RequestMethod.POST)
    @ResponseBody
    public String buy(
            @RequestParam int stockId,
            @RequestParam Long amount
    ) {
        try {
            stockDao.addAmount(stockId, -amount);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return "";
    }

    @RequestMapping(value = "/stock", method = RequestMethod.GET)
    public String getStocks(
            @RequestParam int stockId
    ) {
        return stockDao.getStock(stockId).toString();
    }

    @RequestMapping(value = "/price", method = RequestMethod.GET)
    public String getPrice(
            @RequestParam int stockId
    ) {
        stockDao.getStock(stockId);
        return stockDao.getStock(stockId).getPrice().toString();
    }

    @RequestMapping(value = "/admin/change_price", method = RequestMethod.POST)
    public String changePrice(
            @RequestParam int stockId,
            @RequestParam Long newPrice
    ) {
        stockDao.changePrice(stockId, newPrice);
        return "";
    }

}
