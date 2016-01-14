package main.models;

import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * alex on 14.01.16.
 */
public class UserModel {

    private int id;
    private final String about;
    private final String email;
    private List<String> followers;
    private List<String> following;
    private List<Integer> subscriptions;
    private final boolean isAnonymous;
    private final String name;
    private final String username;

    public UserModel(int id, String email, String name, String username, String about, boolean isAnonymous,
                     List<String> followers, List<String> following, List<Integer> subscriptions) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.username = username;
        this.about = about;
        this.isAnonymous = isAnonymous;
        this.followers = followers;
        this.following = following;
        this.subscriptions = subscriptions;
    }

    public UserModel(String email, String name, String username, String about, boolean isAnonymous) {
        this(-1, email, name, username, about, isAnonymous, new ArrayList<String>(), new ArrayList<String>(),
                new ArrayList<Integer>());
    }

    public UserModel(JsonObject jsonObject) throws Exception {
        this(
                jsonObject.get("email").getAsString(),
                jsonObject.get("name").isJsonNull() ? null : jsonObject.get("name").getAsString(),
                jsonObject.get("username").isJsonNull() ? null : jsonObject.get("username").getAsString(),
                jsonObject.get("about").isJsonNull() ? null : jsonObject.get("about").getAsString(),
                jsonObject.has("isAnonymous") && jsonObject.get("isAnonymous").getAsBoolean()
        );
    }

    public UserModel(int id, String email, String name, String username, String about, boolean isAnonymous) {
        this(id, email, name, username, about, isAnonymous, null, null, null);
    }

    public UserModel(ResultSet resultSet) throws SQLException {
        this(
                resultSet.getInt("id"),
                resultSet.getString("email"),
                resultSet.getString("name"),
                resultSet.getString("username"),
                resultSet.getString("about"),
                resultSet.getBoolean("isAnonymous")
        );
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAbout() {
        return about;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> array) {
        this.followers = array;
    }

    public List<String> getFollowing() {
        return following;
    }

    public void setFollowing(List<String> array) {
        this.following = array;
    }

    public List<Integer> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Integer> array) {
        this.subscriptions = array;
    }

    public boolean getIsAnonymous() {
        return isAnonymous;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }
}
