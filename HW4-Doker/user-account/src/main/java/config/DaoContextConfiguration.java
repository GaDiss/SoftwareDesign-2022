package config;

import dao.InMemoryUserDao;
import dao.UserDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoContextConfiguration {
    @Bean
    public UserDao userController() {
        return new InMemoryUserDao();
    }
}
