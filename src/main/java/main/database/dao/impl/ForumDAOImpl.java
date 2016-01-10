package main.database.dao.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import main.database.dao.ForumDAO;
import main.database.executor.TExecutor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;

/**
 * alex on 03.01.16.
 */
public class ForumDAOImpl implements ForumDAO {

    private final DataSource dataSource;

    public ForumDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int getCount() {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT COUNT(*) FROM forum;";
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
        try (Connection connection = dataSource.getConnection()) {
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE forum;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String create(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        int forumId = -1;

        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT INTO forum (name, short_name, user) VALUES (?,?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, object.get("name").getAsString());
                preparedStatement.setString(2, object.get("short_name").getAsString());
                preparedStatement.setString(3, object.get("user").getAsString());
                preparedStatement.execute();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        forumId = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        object.addProperty("id", forumId);
        return object.toString();
    }

    @Override
    public String details(String forum, String[] related) {
        JsonObject object = new JsonObject();

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM forum WHERE short_name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, forum);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    object.addProperty("id", resultSet.getInt("id"));
                    object.addProperty("name", resultSet.getString("name"));
                    object.addProperty("short_name", resultSet.getString("short_name"));
                    object.addProperty("user", resultSet.getString("user"));
                    if (related != null) {
                        if (Arrays.asList(related).contains("user")) {
                            object.add("user",
                                    new JsonParser().parse(
                                            new UserDAOImpl(dataSource).details(object.get("user").getAsString())
                                    ).getAsJsonObject()
                            );
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    @Override
    public String listPosts(String forum, String since, Integer limit, String order, String[] related) {
        JsonArray array = new JsonArray();
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT * FROM post ");
        queryBuilder.append("WHERE forum = ?");
        if (since != null) {
            queryBuilder.append(" AND date >= ?");
        }
        queryBuilder.append(" ORDER BY date ");
        if (order != null) {
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
            queryBuilder.append("DESC");
        }
        if (limit != null) {
            queryBuilder.append(" LIMIT ?");
        }
        queryBuilder.append(';');

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 0;
                preparedStatement.setString(++parameterIndex, forum);
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
                        if (related != null) {
                            if (Arrays.asList(related).contains("user")) {
                                object.add("user",
                                        new JsonParser().parse(
                                                new UserDAOImpl(dataSource).details(object.get("user").getAsString())
                                        ).getAsJsonObject()
                                );
                            }
                            if (Arrays.asList(related).contains("forum")) {
                                object.add("forum",
                                        new JsonParser().parse(
                                                details(object.get("forum").getAsString(), null)
                                        ).getAsJsonObject()
                                );
                            }
                            if (Arrays.asList(related).contains("thread")) {
                                object.add("thread",
                                        new JsonParser().parse(
                                                new ThreadDAOImpl(dataSource).details(object.get("thread").getAsInt(), null)
                                        ).getAsJsonObject()
                                );
                            }
                        }
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
    public String listThreads(String forum, String since, Integer limit, String order, String[] related) {
        JsonArray array = new JsonArray();
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT * FROM thread ");
        queryBuilder.append("WHERE forum = ?");
        if (since != null) {
            queryBuilder.append(" AND date >= ?");
        }
        queryBuilder.append(" ORDER BY date ");
        if (order != null) {
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
            queryBuilder.append("DESC");
        }
        if (limit != null) {
            queryBuilder.append(" LIMIT ?");
        }
        queryBuilder.append(';');

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 0;
                preparedStatement.setString(++parameterIndex, forum);
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
                        object.addProperty("title", resultSet.getString("title"));
                        object.addProperty("slug", resultSet.getString("slug"));
                        object.addProperty("id", resultSet.getInt("id"));
                        object.addProperty("user", resultSet.getString("user"));
                        object.addProperty("isClosed", resultSet.getBoolean("isClosed"));
                        object.addProperty("isDeleted", resultSet.getBoolean("isDeleted"));
                        if (related != null) {
                            if (Arrays.asList(related).contains("user")) {
                                object.add("user",
                                        new JsonParser().parse(
                                                new UserDAOImpl(dataSource).details(object.get("user").getAsString())
                                        ).getAsJsonObject()
                                );
                            }
                            if (Arrays.asList(related).contains("forum")) {
                                object.add("forum",
                                        new JsonParser().parse(
                                                details(object.get("forum").getAsString(), null)
                                        ).getAsJsonObject()
                                );
                            }
                        }
                        object.addProperty("posts", new PostDAOImpl(dataSource).getCount(object.get("id").getAsInt()));
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
    public String listUsers(String forum, Integer sinceId, Integer limit, String order) {
        JsonArray array = new JsonArray();
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT * FROM user ");
        queryBuilder.append("WHERE email IN (");
        queryBuilder.append("SELECT DISTINCT user FROM post WHERE forum = ?");
        queryBuilder.append(')');
        if (sinceId != null) {
            queryBuilder.append(" AND id >= ?");
        }
        queryBuilder.append(" ORDER BY name ");
        if (order != null) {
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
            queryBuilder.append("DESC");
        }
        if (limit != null) {
            queryBuilder.append(" LIMIT ?");
        }
        queryBuilder.append(';');

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 0;
                preparedStatement.setString(++parameterIndex, forum);
                if (sinceId != null) {
                    preparedStatement.setInt(++parameterIndex, sinceId);
                }
                if (limit != null) {
                    preparedStatement.setInt(++parameterIndex, limit);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        JsonObject object = new JsonObject();
                        String email = resultSet.getString("email");
                        object.addProperty("id", resultSet.getInt("id"));
                        object.addProperty("name", resultSet.getString("name"));
                        object.addProperty("username", resultSet.getString("username"));
                        object.addProperty("email", email);
                        object.addProperty("about", resultSet.getString("about"));
                        object.addProperty("isAnonymous", resultSet.getBoolean("isAnonymous"));
                        object.add("followers", new UserDAOImpl(dataSource).getFollowers(connection, email));
                        object.add("following", new UserDAOImpl(dataSource).getFollowing(connection, email));
                        object.add("subscriptions", new UserDAOImpl(dataSource).getSubscriptions(connection, email));
                        array.add(object);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return array.toString();
    }
}
