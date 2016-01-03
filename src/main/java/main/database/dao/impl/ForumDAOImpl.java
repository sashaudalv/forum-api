package main.database.dao.impl;

import main.database.dao.ForumDAO;

import java.sql.Connection;

/**
 * alex on 03.01.16.
 */
public class ForumDAOImpl implements ForumDAO {

    public static final String TABLE_NAME = "forum";

    private final Connection connection;

    public ForumDAOImpl(Connection connection) {
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
