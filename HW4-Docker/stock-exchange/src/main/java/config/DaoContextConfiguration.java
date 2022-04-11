package config;

import dao.InMemoryStockDao;
import dao.StockDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoContextConfiguration {
    @Bean
    public StockDao stockDao() {
        return new InMemoryStockDao();
    }
}
