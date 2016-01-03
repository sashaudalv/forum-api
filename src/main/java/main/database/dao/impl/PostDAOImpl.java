package main.database.dao.impl;

import main.database.dao.PostDAO;

import java.sql.Connection;

/**
 * alex on 03.01.16.
 */
public class PostDAOImpl implements PostDAO {

    public static final String TABLE_NAME = "post";

    private final Connection connection;

    public PostDAOImpl(Connection connection) {
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
