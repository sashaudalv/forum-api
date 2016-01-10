package main.database.dao.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import main.database.dao.PostDAO;
import main.database.executor.TExecutor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;

/**
 * alex on 03.01.16.
 */
public class PostDAOImpl implements PostDAO {

    private static final int BASE_36 = 36;

    private final DataSource dataSource;

    public PostDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int getCount() {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT COUNT(*) FROM post;";
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
            TExecutor.execQuery(connection, "TRUNCATE TABLE post;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    @Override
    public int getCount(int threadId) {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT COUNT(*) FROM post WHERE thread = ? AND isDeleted = 0;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, threadId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public String create(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        if (!object.has("isApproved")) {
            object.addProperty("isApproved", false);
        }
        if (!object.has("isHighlighted")) {
            object.addProperty("isHighlighted", false);
        }
        if (!object.has("isEdited")) {
            object.addProperty("isEdited", false);
        }
        if (!object.has("isSpam")) {
            object.addProperty("isSpam", false);
        }
        if (!object.has("isDeleted")) {
            object.addProperty("isDeleted", false);
        }
        if (!object.has("parent")) {
            object.add("parent", null);
        }

        int postId = -1;

        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT INTO post (date, thread, message, user, forum, parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, object.get("date").getAsString());
                preparedStatement.setInt(2, object.get("thread").getAsInt());
                preparedStatement.setString(3, object.get("message").getAsString());
                preparedStatement.setString(4, object.get("user").getAsString());
                preparedStatement.setString(5, object.get("forum").getAsString());
                if (object.get("parent").isJsonNull()) {
                    preparedStatement.setObject(6, null);
                } else {
                    preparedStatement.setInt(6, object.get("parent").getAsInt());
                }
                preparedStatement.setBoolean(7, object.get("isApproved").getAsBoolean());
                preparedStatement.setBoolean(8, object.get("isHighlighted").getAsBoolean());
                preparedStatement.setBoolean(9, object.get("isEdited").getAsBoolean());
                preparedStatement.setBoolean(10, object.get("isSpam").getAsBoolean());
                preparedStatement.setBoolean(11, object.get("isDeleted").getAsBoolean());
                preparedStatement.execute();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        postId = resultSet.getInt(1);
                    }
                }
            }

            String mPath = "";

            if (!object.get("parent").isJsonNull()) {
                String mPathQuery = "SELECT mpath FROM post WHERE id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(mPathQuery)) {
                    preparedStatement.setInt(1, object.get("parent").getAsInt());
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            mPath = resultSet.getString(1);
                        }
                    }
                }
            }

            mPath += '/';
            mPath += Integer.toString(postId, BASE_36);

            String mPathUpdateQuery = "UPDATE post SET mpath = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(mPathUpdateQuery)) {
                preparedStatement.setString(1, mPath);
                preparedStatement.setInt(2, postId);
                preparedStatement.execute();
            }

            String threadUpdateQuery = "UPDATE thread SET posts = posts + 1 WHERE id = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(threadUpdateQuery)) {
                preparedStatement.setInt(1, object.get("thread").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        object.addProperty("id", postId);
        return object.toString();
    }

    @Override
    public String details(int postId, String[] related) {
        JsonObject object = new JsonObject();

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM post WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, postId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
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
                                                new ForumDAOImpl(dataSource).details(object.get("forum").getAsString(), null)
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
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    @Override
    public String listForumPosts(String forum, String since, Integer limit, String order) {
        return new ForumDAOImpl(dataSource).listPosts(forum, since, limit, order, null);
    }

    @Override
    public String listThreadPosts(int threadId, String since, Integer limit, String order) {
        return new ThreadDAOImpl(dataSource).listPosts(threadId, since, limit, null, order);
    }

    @Override
    public String remove(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        try (Connection connection = dataSource.getConnection()) {
            String query = "UPDATE post SET isDeleted = 1 WHERE id = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("post").getAsInt());
                preparedStatement.execute();
            }

            String threadUpdateQuery = "UPDATE thread SET posts = posts - 1 WHERE id = (SELECT thread from post WHERE id = ?);";
            try (PreparedStatement preparedStatement = connection.prepareStatement(threadUpdateQuery)) {
                preparedStatement.setInt(1, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    @Override
    public String restore(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        try (Connection connection = dataSource.getConnection()) {
            String query = "UPDATE post SET isDeleted = 0 WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("post").getAsInt());
                preparedStatement.execute();
            }

            String threadUpdateQuery = "UPDATE thread SET posts = posts + 1 WHERE id = (SELECT thread from post WHERE id = ?);";
            try (PreparedStatement preparedStatement = connection.prepareStatement(threadUpdateQuery)) {
                preparedStatement.setInt(1, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    @Override
    public String update(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        try (Connection connection = dataSource.getConnection()) {
            String query = "UPDATE post SET message = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, object.get("message").getAsString());
                preparedStatement.setInt(2, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details(object.get("post").getAsInt(), null);
    }

    @Override
    public String vote(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        String likeQuery = "UPDATE post SET likes = likes + 1 WHERE id = ?";
        String dislikeQuery = "UPDATE post SET dislikes = dislikes + 1 WHERE id = ?";

        String query = object.get("vote").getAsInt() > 0 ? likeQuery : dislikeQuery;

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details(object.get("post").getAsInt(), null);
    }
}
