package main.database.dao.impl;

import main.database.dao.ThreadDAO;

import java.sql.Connection;

/**
 * alex on 03.01.16.
 */
public class ThreadDAOImpl implements ThreadDAO {

    public static final String TABLE_NAME = "thread";

    private final Connection connection;

    public ThreadDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public void truncateTable() {

    }
}
