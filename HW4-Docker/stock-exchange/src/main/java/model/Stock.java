package model;

public class Stock {
    private int id;
    private final String companyName;
    private Long price;
    private Long amount;

    public Stock(int id, String companyName, Long price, Long amount) {
        this.id = id;
        this.companyName = companyName;
        this.price = price;
        this.amount = amount;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getAmount() {
        return amount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addAmount(Long amount) {
        if (this.amount + amount < 0) throw new AssertionError("Negative stocks");
        this.amount += amount;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", companyName='" + companyName + '\'' +
                ", price=" + price +
                ", amount=" + amount +
                '}';
    }
}
