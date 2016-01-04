package main.database.dao.impl;

import main.database.dao.PostDAO;
import main.database.executor.TExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * alex on 03.01.16.
 */
public class PostDAOImpl implements PostDAO {

    private final Connection connection;

    public PostDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int getCount() {
        try {
            String query = "SELECT COUNT(*) FROM post;";
            return TExecutor.execQuery(connection, query, resultSet -> {
                resultSet.next();
                return resultSet.getInt(1);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void truncateTable() {
        try {
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE post;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount(int threadId) {
        try {
            String query = "SELECT COUNT(*) FROM post WHERE thread = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, threadId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
