package dao;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.RequestFailedException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class HttpStockDao implements StockDao {
    private final UserDao userDao;
    private final HttpClient client;
    private final String url;

    public HttpStockDao(UserDao userDao, String url) {
        this.userDao = userDao;
        this.url = url;
        this.client = HttpClient.newHttpClient();
    }

    public Long calcWorth(Map<Integer, Long> stocksAmount) {
        return stocksAmount.entrySet().stream()
                .map(e -> getPrice(e.getKey()) * e.getValue())
                .reduce(0L, Long::sum);
    }

    public void buy(int userId, int stockId, Long amount) throws URISyntaxException, IOException, InterruptedException {
        long price = getPrice(stockId);

        userDao.addBalance(userId, -amount * price);
        post(url + "/buy?stockId=" + stockId + "&amount=" + amount);
        userDao.addStocks(userId, stockId, amount);
    }

    public Long getPrice(int stockId) {
        try {
            return Long.parseLong(get(url + "/price?stockId=" + stockId));
        } catch (Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }

    private void post(String uri) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new RequestFailedException(response.body());
    }

    private String get(String uri) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new RequestFailedException(response.body());
        return response.body();
    }
}
