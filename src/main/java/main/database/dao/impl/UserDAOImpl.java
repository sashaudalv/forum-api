package main.database.dao.impl;

import main.database.dao.UserDAO;
import main.database.data.UserData;

import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

/**
 * alex on 03.01.16.
 */
public class UserDAOImpl implements UserDAO {

    public static final String TABLE_NAME = "user";

    private final Connection connection;

    public UserDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public void truncateTable() {

    }

    @Override
    public UserData create(String jsonString) {
        return null;
    }

    @Override
    public Map<String, Object> details() {
        return null;
    }

    @Override
    public void follow() {

    }

    @Override
    public void unfollow() {

    }

    @Override
    public Collection<Object> listFollowers() {
        return null;
    }

    @Override
    public Collection<Object> listFollowing() {
        return null;
    }

    @Override
    public Collection<Object> listPosts() {
        return null;
    }

    @Override
    public void updateProfile() {

    }
}
