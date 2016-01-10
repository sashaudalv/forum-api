package main.config;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * alex on 03.01.16.
 */
@Configuration
public class Config {

    private static DataSource dataSource;

    @Bean
    public DataSource getDataSource() {
        if (dataSource != null) {
            return dataSource;
        }

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder builder = new StringBuilder();
        builder.append("jdbc:mysql://localhost:3306/");
        builder.append("forum_api?");
        builder.append("useUnicode=true&characterEncoding=utf8&autoReconnect=true");

        GenericObjectPool genericObjectPool = new GenericObjectPool();
        genericObjectPool.setMaxActive(100);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                builder.toString(),
                "user",
                "password");

        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, genericObjectPool,
                        null, null, false, true);

        dataSource = new PoolingDataSource(genericObjectPool);
        return dataSource;
    }
}
