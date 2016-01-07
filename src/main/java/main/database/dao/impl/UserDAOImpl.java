package main.database.dao.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import main.database.dao.UserDAO;
import main.database.executor.TExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * alex on 03.01.16.
 */
public class UserDAOImpl implements UserDAO {

    private static final int MYSQL_DUPLICATE_PK = 1062;

    private final Connection connection;

    public UserDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int getCount() {
        try {
            String query = "SELECT COUNT(*) FROM user;";
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
            TExecutor.execQuery(connection, "TRUNCATE TABLE user;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE follow;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String create(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        if (!object.has("isAnonymous")) {
            object.addProperty("isAnonymous", false);
        }

        try {
            String query = "INSERT INTO user (username, about, name, email, isAnonymous) VALUES (?,?,?,?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, object.get("username").isJsonNull() ? null : object.get("username").getAsString());
                preparedStatement.setString(2, object.get("about").isJsonNull() ? null : object.get("about").getAsString());
                preparedStatement.setString(3, object.get("name").isJsonNull() ? null : object.get("name").getAsString());
                preparedStatement.setString(4, object.get("email").getAsString());
                preparedStatement.setBoolean(5, object.get("isAnonymous").getAsBoolean());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getErrorCode() == MYSQL_DUPLICATE_PK) {
                return null;
            }
        }
        return details(object.get("email").getAsString());
    }

    @Override
    public String details(String email) {
        JsonObject object = new JsonObject();

        try {
            String query = "SELECT * FROM user WHERE email = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    object.addProperty("id", resultSet.getInt("id"));
                    object.addProperty("name", resultSet.getString("name"));
                    object.addProperty("username", resultSet.getString("username"));
                    object.addProperty("email", resultSet.getString("email"));
                    object.addProperty("about", resultSet.getString("about"));
                    object.addProperty("isAnonymous", resultSet.getBoolean("isAnonymous"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        object.add("followers", getFollowers(email));
        object.add("following", getFollowing(email));
        object.add("subscriptions", getSubscriptions(email));

        return object.toString();
    }

    public JsonArray getFollowers(String email) {
        JsonArray array = new JsonArray();
        try {
            String query = "SELECT follower FROM follow WHERE followee = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        array.add(resultSet.getString("follower"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return array;
    }

    public JsonArray getFollowing(String email) {
        JsonArray array = new JsonArray();
        try {
            String query = "SELECT followee FROM follow WHERE follower = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        array.add(resultSet.getString("followee"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return array;
    }

    public JsonArray getSubscriptions(String email) {
        JsonArray array = new JsonArray();
        try {
            String query = "SELECT thread FROM subscribe WHERE user = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        array.add(resultSet.getInt("thread"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return array;
    }

    @Override
    public void follow(String follower, String followee) {
        try {
            String query = "INSERT INTO follow (follower, followee) VALUES (?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, follower);
                preparedStatement.setString(2, followee);
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unfollow(String follower, String followee) {
        try {
            String query = "DELETE FROM follow WHERE follower = ? AND followee = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, follower);
                preparedStatement.setString(2, followee);
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String listFollowers(String email, Integer limit, String order, Integer sinceId) {
        JsonArray array = new JsonArray();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT u.* FROM user u ");
        queryBuilder.append("INNER JOIN follow f ON u.email = f.follower ");
        queryBuilder.append("WHERE followee = ?");
        if (sinceId != null) {
            queryBuilder.append(" AND u.id >= ?");
        }
        if (order != null) {
            queryBuilder.append(" ORDER BY u.name ");
            switch (order) {
                case "asc":
                    queryBuilder.append("ASC");
                    break;
                case "desc":
                    queryBuilder.append("DESC");
                    break;
                default:
                    queryBuilder.append("DESC");
            }
        } else {
            queryBuilder.append(" ORDER BY u.name DESC");
        }
        if (limit != null) {
            queryBuilder.append(" LIMIT ?");
        }
        queryBuilder.append(';');
        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 0;
                preparedStatement.setString(++parameterIndex, email);
                if (sinceId != null) {
                    preparedStatement.setInt(++parameterIndex, sinceId);
                }
                if (limit != null) {
                    preparedStatement.setInt(++parameterIndex, limit);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        JsonObject object = new JsonObject();
                        String followerEmail = resultSet.getString("email");
                        object.addProperty("id", resultSet.getInt("id"));
                        object.addProperty("name", resultSet.getString("name"));
                        object.addProperty("username", resultSet.getString("username"));
                        object.addProperty("email", followerEmail);
                        object.addProperty("about", resultSet.getString("about"));
                        object.addProperty("isAnonymous", resultSet.getBoolean("isAnonymous"));
                        object.add("followers", getFollowers(followerEmail));
                        object.add("following", getFollowing(followerEmail));
                        object.add("subscriptions", getSubscriptions(followerEmail));
                        array.add(object);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return array.toString();
    }

    @Override
    public String listFollowing(String email, Integer limit, String order, Integer sinceId) {
        JsonArray array = new JsonArray();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT u.* FROM user u ");
        queryBuilder.append("INNER JOIN follow f ON u.email = f.followee ");
        queryBuilder.append("WHERE follower = ?");
        if (sinceId != null) {
            queryBuilder.append(" AND u.id >= ?");
        }
        if (order != null) {
            queryBuilder.append(" ORDER BY u.name ");
            switch (order) {
                case "asc":
                    queryBuilder.append("ASC");
                    break;
                case "desc":
                    queryBuilder.append("DESC");
                    break;
                default:
                    queryBuilder.append("DESC");
            }
        } else {
            queryBuilder.append(" ORDER BY u.name DESC");
        }
        if (limit != null) {
            queryBuilder.append(" LIMIT ?");
        }
        queryBuilder.append(';');
        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 0;
                preparedStatement.setString(++parameterIndex, email);
                if (sinceId != null) {
                    preparedStatement.setInt(++parameterIndex, sinceId);
                }
                if (limit != null) {
                    preparedStatement.setInt(++parameterIndex, limit);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        JsonObject object = new JsonObject();
                        String followeeEmail = resultSet.getString("email");
                        object.addProperty("id", resultSet.getInt("id"));
                        object.addProperty("name", resultSet.getString("name"));
                        object.addProperty("username", resultSet.getString("username"));
                        object.addProperty("email", followeeEmail);
                        object.addProperty("about", resultSet.getString("about"));
                        object.addProperty("isAnonymous", resultSet.getBoolean("isAnonymous"));
                        object.add("followers", getFollowers(followeeEmail));
                        object.add("following", getFollowing(followeeEmail));
                        object.add("subscriptions", getSubscriptions(followeeEmail));
                        array.add(object);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return array.toString();
    }

    @Override
    public String listPosts(String email, Integer limit, String order, String since) {
        JsonArray array = new JsonArray();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM post ");
        queryBuilder.append("WHERE user = ?");
        if (since != null) {
            queryBuilder.append(" AND date >= ?");
        }
        if (order != null) {
            queryBuilder.append(" ORDER BY date ");
            switch (order) {
                case "asc":
                    queryBuilder.append("ASC");
                    break;
                case "desc":
                    queryBuilder.append("DESC");
                    break;
                default:
                    queryBuilder.append("DESC");
            }
        } else {
            queryBuilder.append(" ORDER BY date DESC");
        }
        if (limit != null) {
            queryBuilder.append(" LIMIT ?");
        }
        queryBuilder.append(';');
        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 0;
                preparedStatement.setString(++parameterIndex, email);
                if (since != null) {
                    preparedStatement.setString(++parameterIndex, since);
                }
                if (limit != null) {
                    preparedStatement.setInt(++parameterIndex, limit);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        JsonObject object = new JsonObject();
                        String date = resultSet.getString("date");
                        object.addProperty("date", date.substring(0, date.length() - 2));
                        int likes = resultSet.getInt("likes");
                        object.addProperty("likes", likes);
                        int dislikes = resultSet.getInt("dislikes");
                        object.addProperty("dislikes", dislikes);
                        object.addProperty("points", likes - dislikes);
                        object.addProperty("forum", resultSet.getString("forum"));
                        object.addProperty("message", resultSet.getString("message"));
                        object.addProperty("parent", (Integer) resultSet.getObject("parent"));
                        object.addProperty("thread", resultSet.getInt("thread"));
                        object.addProperty("id", resultSet.getInt("id"));
                        object.addProperty("user", resultSet.getString("user"));
                        object.addProperty("isApproved", resultSet.getBoolean("isApproved"));
                        object.addProperty("isDeleted", resultSet.getBoolean("isDeleted"));
                        object.addProperty("isEdited", resultSet.getBoolean("isEdited"));
                        object.addProperty("isHighlighted", resultSet.getBoolean("isHighlighted"));
                        object.addProperty("isSpam", resultSet.getBoolean("isSpam"));
                        array.add(object);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return array.toString();
    }

    @Override
    public void updateProfile(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();
        try {
            String query = "UPDATE user SET about = ?, name = ? WHERE email = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, object.get("about").getAsString());
                preparedStatement.setString(2, object.get("name").getAsString());
                preparedStatement.setString(3, object.get("user").getAsString());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
