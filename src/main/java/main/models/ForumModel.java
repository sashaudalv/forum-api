package main.models;

import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * alex on 14.01.16.
 */
public class ForumModel {

    private int id;
    private final String name;
    private final String short_name;
    private Object user;

    public ForumModel(int id, String name, String shortName, Object user) {
        this.id = id;
        this.name = name;
        this.short_name = shortName;
        this.user = user;
    }

    public ForumModel(String name, String shortName, Object user) {
        this(-1, name, shortName, user);
    }

    public ForumModel(JsonObject jsonObject) throws Exception {
        this(
                jsonObject.get("name").getAsString(),
                jsonObject.get("short_name").getAsString(),
                jsonObject.get("user").getAsString()
        );
    }

    public ForumModel(ResultSet resultSet) throws SQLException {
        this(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("short_name"),
                resultSet.getString("user")
        );
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getShort_name() {
        return short_name;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }
}
