import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.RequestFailedException;
import controller.UserController;
import dao.InMemoryUserDao;
import model.Stock;
import org.junit.*;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.Assert.*;

public class AppTest {

    @ClassRule
    public static GenericContainer simpleWebServer
            = new FixedHostPortGenericContainer("stock-exchange:1.0-SNAPSHOT")
            .withFixedExposedPort(8080, 8080)
            .withExposedPorts(8080);

    private UserController controller;

    @Before
    public void initController() {
        controller = new UserController(new InMemoryUserDao());
    }

    @Test
    public void addUser() {
        int uid = Integer.parseInt(controller.addUser("a", 123L));
        assertEquals("123", controller.getBalance(uid));
        assertEquals("{}", controller.userStocks(uid));
        assertEquals("123", controller.userWorth(uid));
    }

    @Test
    public void addCompany() throws IOException, URISyntaxException, InterruptedException {
        int sid = Integer.parseInt(post("add_company?companyName=foo&price=123&amount=456"));
        assertEquals("123", get("price?stockId=" + sid));
        assertEquals(
                new Stock(sid, "foo", 123L, 456L).toString(),
                get("stock?stockId=" + sid)
        );
    }

    @Test
    public void addAndDecreaseStock() throws IOException, URISyntaxException, InterruptedException {
        int sid = Integer.parseInt(post("add_company?companyName=foo&price=123&amount=456"));
        Stock stock = new Stock(sid, "foo", 123L, 456L);
        assertEquals(stock.toString(), get("stock?stockId=" + sid));

        stock.addAmount(544L);
        post("add_amount?stockId=" + sid + "&amount=544");
        assertEquals(stock.toString(), get("stock?stockId=" + sid));

        stock.addAmount(-1000L);
        post("add_amount?stockId=" + sid + "&amount=-1000");
        assertEquals(stock.toString(), get("stock?stockId=" + sid));

        post("add_amount?stockId=" + sid + "&amount=-1000", 500);
    }

    @Test
    public void addAndDecreaseBalance() {
        int uid = Integer.parseInt(controller.addUser("a", 1L));

        controller.addBalance(uid, 1L);
        assertEquals("2", controller.getBalance(uid));

        controller.addBalance(uid, -2L);
        assertEquals("0", controller.getBalance(uid));

        assertThrows(AssertionError.class, () -> controller.addBalance(uid, -1L));
    }

    @Test
    public void userWorth() throws IOException, URISyntaxException, InterruptedException {
        int sid1 = Integer.parseInt(post("add_company?companyName=a&price=2&amount=100"));
        int sid2 = Integer.parseInt(post("add_company?companyName=b&price=0&amount=100"));
        int uid = Integer.parseInt(controller.addUser("sergey", 100L));

        assertEquals("100", controller.userWorth(uid));

        controller.buy(uid, sid1, 10L);
        assertEquals("100", controller.userWorth(uid));

        controller.addBalance(uid, -5L);
        assertEquals("95", controller.userWorth(uid));

        controller.buy(uid, sid2, 10L);
        assertEquals("95", controller.userWorth(uid));

        post("admin/change_price?stockId=" + sid2 + "&newPrice=10");
        assertEquals("195", controller.userWorth(uid));

        controller.buy(uid, sid1, -9L);
        assertEquals("195", controller.userWorth(uid));

        post("admin/change_price?stockId=" + sid1 + "&newPrice=1");
        assertEquals("194", controller.userWorth(uid));
    }

    @Test
    public void createTwo() throws IOException, URISyntaxException, InterruptedException {
        int sid1 = Integer.parseInt(post("add_company?companyName=a&price=0&amount=0"));
        int sid2 = Integer.parseInt(post("add_company?companyName=b&price=0&amount=0"));
        assertNotEquals(sid1, sid2);

        int uid1 = Integer.parseInt(controller.addUser("a", 0L));
        int uid2 = Integer.parseInt(controller.addUser("b", 0L));
        assertNotEquals(uid1, uid2);
    }

    @Test
    public void testLegitBuyAndSell() throws Exception {
        int sid = Integer.parseInt(post("add_company?companyName=a&price=1&amount=10"));
        int uid = Integer.parseInt(controller.addUser("a", 100L));

        controller.buy(uid, sid, 10L);

        assertEquals(
                new Stock(sid, "a", 1L, 0L).toString(),
                get("stock?stockId=" + sid)
        );
        assertEquals("{" + sid + ": {amount: 10, price: 1}}", controller.userStocks(uid));
        assertEquals("90", controller.getBalance(uid));

        post("admin/change_price?stockId=" + sid + "&newPrice=2");

        controller.buy(uid, sid, -5L);
        assertEquals(
                new Stock(sid, "a", 2L, 5L).toString(),
                get("stock?stockId=" + sid)
        );
        assertEquals("{" + sid + ": {amount: 5, price: 2}}", controller.userStocks(uid));
        assertEquals("100", controller.getBalance(uid));
    }

    @Test
    public void stockIdDoesNotExist() throws Exception {
        get("stock?stockId=112", 500);
    }

    @Test
    public void testNotLegitBuy() throws Exception {
        int sid = Integer.parseInt(post("add_company?companyName=a&price=2&amount=10"));
        int uid = Integer.parseInt(controller.addUser("a", 5L));

        assertThrows(AssertionError.class, () -> controller.buy(uid, sid, 10L));

        controller.addBalance(uid, 395L);
        assertThrows(RequestFailedException.class, () -> controller.buy(uid, sid, 11L));
    }

    @Test
    public void test() {
        
    }

    private String post(String uri) throws IOException, InterruptedException, URISyntaxException {
        return post(uri, 200);
    }

    private String post(String uri, int responseCode) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/" + uri))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(responseCode, response.statusCode());
        return response.body();
    }

    private String get(String uri) throws IOException, InterruptedException, URISyntaxException {
        return get(uri, 200);
    }

    private String get(String uri, int responseCode) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/" + uri))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(responseCode, response.statusCode());
        return response.body();
    }

}
