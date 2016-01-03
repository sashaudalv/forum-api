package main.config;

import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * alex on 03.01.16.
 */
@Configuration
public class Config {

    private static Connection connection;

    @Bean
    public Connection getConnection() {
        if (connection != null) {
            return connection;
        }

        try {
            DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());

            StringBuilder builder = new StringBuilder();
            builder.append("jdbc:mysql://localhost:3306/");
            builder.append("forum_api?");
            builder.append("user=user&");
            builder.append("password=password&");
            builder.append("useUnicode=true&characterEncoding=utf8&autoReconnect=true");

            connection = DriverManager.getConnection(builder.toString());
            return connection;
        } catch (CommunicationsException e) {
            System.out.println("Communication to mysql occurred. Maybe you have not installed mysql server.");
            e.printStackTrace();
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            System.out.println("Error while contecting to database was occurred.");
            System.out.println("Check your database server settings and database configs.");
            e.printStackTrace();
        }
        return null;
    }
}
