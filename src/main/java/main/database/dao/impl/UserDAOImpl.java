package main.database.dao.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import main.database.dao.UserDAO;
import main.database.executor.TExecutor;
import main.models.PostModel;
import main.models.Response;
import main.models.UserModel;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * alex on 03.01.16.
 */
public class UserDAOImpl implements UserDAO {

    private static final int MYSQL_DUPLICATE_PK = 1062;

    private final DataSource dataSource;

    public UserDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int getCount() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT COUNT(*) FROM user;";
                return TExecutor.execQuery(connection, query, resultSet -> {
                    resultSet.next();
                    return resultSet.getInt(1);
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void truncateTable() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
                TExecutor.execQuery(connection, "TRUNCATE TABLE user;");
                TExecutor.execQuery(connection, "TRUNCATE TABLE follow;");
                TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response create(String jsonString) {
        JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        UserModel user;
        try {
            user = new UserModel(object);
        } catch (Exception e) {
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT INTO user (username, about, name, email, isAnonymous) VALUES (?,?,?,?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getAbout());
                preparedStatement.setString(3, user.getName());
                preparedStatement.setString(4, user.getEmail());
                preparedStatement.setBoolean(5, user.getIsAnonymous());
                preparedStatement.execute();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        user.setId(resultSet.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getErrorCode() == MYSQL_DUPLICATE_PK) {
                return new Response(Response.Codes.USER_ALREDY_EXIST);
            } else {
                return new Response(Response.Codes.UNKNOWN_ERROR);
            }
        }

        return new Response(user);
    }

    @Override
    public Response details(String email) {
        UserModel user;

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM user WHERE email = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        user = new UserModel(resultSet);
                    } else {
                        return new Response(Response.Codes.NOT_FOUND);
                    }
                }
            }

            user.setFollowers(getFollowers(connection, email));
            user.setFollowing(getFollowing(connection, email));
            user.setSubscriptions(getSubscriptions(connection, email));
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.UNKNOWN_ERROR);
        }

        return new Response(user);
    }

    public List<String> getFollowers(Connection connection, String email) throws SQLException {
        List<String> array = new ArrayList<>();

        String query = "SELECT follower FROM follow WHERE followee = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    array.add(resultSet.getString("follower"));
                }
            }
        }
        return array;
    }

    public List<String> getFollowing(Connection connection, String email) throws SQLException {
        List<String> array = new ArrayList<>();

        String query = "SELECT followee FROM follow WHERE follower = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    array.add(resultSet.getString("followee"));
                }
            }
        }
        return array;
    }

    public List<Integer> getSubscriptions(Connection connection, String email) throws SQLException {
        List<Integer> array = new ArrayList<>();

        String query = "SELECT thread FROM subscribe WHERE user = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    array.add(resultSet.getInt("thread"));
                }
            }
        }
        return array;
    }

    @Override
    public Response follow(String jsonString) {
        JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT IGNORE INTO follow (follower, followee) VALUES (?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, object.get("follower").getAsString());
                preparedStatement.setString(2, object.get("followee").getAsString());
                preparedStatement.execute();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return details(object.get("follower").getAsString());
    }

    @Override
    public Response unfollow(String jsonString) {
        JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        try {
            try (Connection connection = dataSource.getConnection()) {
                String query = "DELETE FROM follow WHERE follower = ? AND followee = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, object.get("follower").getAsString());
                    preparedStatement.setString(2, object.get("followee").getAsString());
                    preparedStatement.execute();
                }
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return details(object.get("follower").getAsString());
    }

    @Override
    public Response listFollowers(String email, Integer limit, String order, Integer sinceId) {
        List<UserModel> array = new ArrayList<>();

        order = order == null ? "desc" : order;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT u.* FROM user u ");
        queryBuilder.append("INNER JOIN follow f ON u.email = f.follower ");
        queryBuilder.append("WHERE followee = ? ");
        if (sinceId != null) {
            queryBuilder.append("AND u.id >= ? ");
        }
        queryBuilder.append("ORDER BY u.name ");
        switch (order) {
            case "asc":
                queryBuilder.append("ASC");
                break;
            case "desc":
                queryBuilder.append("DESC");
                break;
            default:
                return new Response(Response.Codes.INCORRECT_QUERY);
        }
        if (limit != null) {
            queryBuilder.append(" LIMIT ?");
        }
        queryBuilder.append(';');

        try (Connection connection = dataSource.getConnection()) {
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
                        UserModel follower = new UserModel(resultSet);
                        follower.setFollowers(getFollowers(connection, follower.getEmail()));
                        follower.setFollowing(getFollowing(connection, follower.getEmail()));
                        follower.setSubscriptions(getSubscriptions(connection, follower.getEmail()));
                        array.add(follower);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return new Response(array);
    }

    @Override
    public Response listFollowing(String email, Integer limit, String order, Integer sinceId) {
        List<UserModel> array = new ArrayList<>();

        order = order == null ? "desc" : order;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT u.* FROM user u ");
        queryBuilder.append("INNER JOIN follow f ON u.email = f.followee ");
        queryBuilder.append("WHERE follower = ?");
        if (sinceId != null) {
            queryBuilder.append(" AND u.id >= ?");
        }
        queryBuilder.append(" ORDER BY u.name ");
        switch (order) {
            case "asc":
                queryBuilder.append("ASC");
                break;
            case "desc":
                queryBuilder.append("DESC");
                break;
            default:
                return new Response(Response.Codes.INCORRECT_QUERY);
        }
        if (limit != null) {
            queryBuilder.append(" LIMIT ?");
        }
        queryBuilder.append(';');

        try (Connection connection = dataSource.getConnection()) {
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
                        UserModel followee = new UserModel(resultSet);
                        followee.setFollowers(getFollowers(connection, followee.getEmail()));
                        followee.setFollowing(getFollowing(connection, followee.getEmail()));
                        followee.setSubscriptions(getSubscriptions(connection, followee.getEmail()));
                        array.add(followee);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return new Response(array);
    }

    @Override
    public Response listPosts(String email, Integer limit, String order, String since) {
        List<PostModel> array = new ArrayList<>();

        order = order == null ? "desc" : order;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM post ");
        queryBuilder.append("WHERE user = ?");
        if (since != null) {
            queryBuilder.append(" AND date >= ?");
        }
        queryBuilder.append(" ORDER BY date ");
        switch (order) {
            case "asc":
                queryBuilder.append("ASC");
                break;
            case "desc":
                queryBuilder.append("DESC");
                break;
            default:
                return new Response(Response.Codes.INCORRECT_QUERY);
        }
        if (limit != null) {
            queryBuilder.append(" LIMIT ?");
        }
        queryBuilder.append(';');

        try (Connection connection = dataSource.getConnection()) {
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
                        PostModel post = new PostModel(resultSet);
                        array.add(post);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return new Response(array);
    }

    @Override
    public Response updateProfile(String jsonString) {
        JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "UPDATE user SET about = ?, name = ? WHERE email = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, object.get("about").getAsString());
                preparedStatement.setString(2, object.get("name").getAsString());
                preparedStatement.setString(3, object.get("user").getAsString());
                preparedStatement.execute();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return new Response(details(object.get("user").getAsString()));
    }
}
