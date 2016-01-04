package main.database.dao.impl;

import main.database.dao.ThreadDAO;
import main.database.executor.TExecutor;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * alex on 03.01.16.
 */
public class ThreadDAOImpl implements ThreadDAO {

    private final Connection connection;

    public ThreadDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int getCount() {
        try {
            String query = "SELECT COUNT(*) FROM thread;";
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
            TExecutor.execQuery(connection, "TRUNCATE TABLE thread;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE subscribe;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
