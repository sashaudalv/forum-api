package main.database.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * alex on 03.01.16.
 */
public interface TResultHandler<T> {
    T handle(ResultSet resultSet) throws SQLException;
}
