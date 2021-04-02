package org.jacklamb.luckydata;

import com.lucky.datasource.sql.HikariCPDataSource;
import com.lucky.datasource.sql.LuckyDataSource;
import org.jacklamb.luckydata.test.mapper.BookMapper;
import org.jacklamb.luckydata.test.service.BookService;
import org.luckyframework.context.ApplicationContext;
import org.luckyframework.context.RootBasedAnnotationApplicationContext;
import org.luckyframework.context.annotation.Bean;
import org.luckyframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Hello world!
 *
 */
@Configuration
public class App {

    @Bean
    public LuckyDataSource hikariCPDataSource(){
        HikariCPDataSource dataSource = new HikariCPDataSource();
        dataSource.setLog(true);
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/test-1?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        dataSource.setPoolName("mysql-test-1");
        return dataSource;
    }

    public static void main( String[] args ) throws IOException {
        ApplicationContext context = new RootBasedAnnotationApplicationContext(App.class);
        BookService service = context.getBean(BookService.class);
        service.printAllBook();
        BookMapper mapper = context.getBean(BookMapper.class);
        System.out.println(mapper.findNameByPriceLessThanEqual(25.0));
        context.close();
    }
}
