package main.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * alex on 03.01.16.
 */
@Configuration
@PropertySource("classpath:db.properties")
public class Config {

    @Resource
    private Environment env;

    @Bean
    public DataSource getDataSource() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(env.getProperty("db.driver"));
        basicDataSource.setUrl(env.getProperty("db.url"));
        basicDataSource.setUsername(env.getProperty("db.username"));
        basicDataSource.setPassword(env.getProperty("db.password"));
        basicDataSource.setMaxActive(Integer.parseInt(env.getProperty("db.max-active")));
        basicDataSource.setInitialSize(Integer.parseInt(env.getProperty("db.initial-size")));
        basicDataSource.setValidationQuery(env.getProperty("db.validation-query"));
        return basicDataSource;
    }
}
