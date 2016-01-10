package main.database.dao.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import main.database.dao.ThreadDAO;
import main.database.executor.TExecutor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * alex on 03.01.16.
 */
public class ThreadDAOImpl implements ThreadDAO {

    private final DataSource dataSource;

    public ThreadDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int getCount() {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT COUNT(*) FROM thread;";
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
            TExecutor.execQuery(connection, "TRUNCATE TABLE thread;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE subscribe;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String create(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        if (!object.has("isDeleted")) {
            object.addProperty("isDeleted", false);
        }

        int threadId = -1;

        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT INTO thread (forum, title, isClosed, user, date, message, slug, isDeleted) VALUES (?,?,?,?,?,?,?,?);";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, object.get("forum").getAsString());
                preparedStatement.setString(2, object.get("title").getAsString());
                preparedStatement.setBoolean(3, object.get("isClosed").getAsBoolean());
                preparedStatement.setString(4, object.get("user").getAsString());
                preparedStatement.setString(5, object.get("date").getAsString());
                preparedStatement.setString(6, object.get("message").getAsString());
                preparedStatement.setString(7, object.get("slug").getAsString());
                preparedStatement.setBoolean(8, object.get("isDeleted").getAsBoolean());
                preparedStatement.execute();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        threadId = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        object.addProperty("id", threadId);
        return object.toString();
    }

    @Override
    public String details(int threadId, String[] related) {
        JsonObject object = new JsonObject();

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM thread WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, threadId);
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
                        object.addProperty("id", resultSet.getInt("id"));
                        object.addProperty("user", resultSet.getString("user"));
                        object.addProperty("slug", resultSet.getString("slug"));
                        object.addProperty("title", resultSet.getString("title"));
                        object.addProperty("isDeleted", resultSet.getBoolean("isDeleted"));
                        object.addProperty("isClosed", resultSet.getBoolean("isClosed"));
                        object.addProperty("posts", resultSet.getInt("posts"));
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
    public String listPosts(int threadId, String since, Integer limit, String sort, String order) {
        JsonArray array = new JsonArray();
        StringBuilder queryBuilder = new StringBuilder();

        if (sort == null || Objects.equals(sort, "flat")) {
            queryBuilder.append("SELECT * FROM post ");
            queryBuilder.append("WHERE thread = ?");
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
        } else {
            String rootQuery = "SELECT mpath FROM post WHERE parent IS NULL AND thread = ? ";
            if (since != null) {
                rootQuery += "AND date >= ? ";
            }
            if (limit != null) {
                rootQuery += "LIMIT ?";
            }
            queryBuilder.append("SELECT * FROM (");
            queryBuilder.append(rootQuery);
            queryBuilder.append(") AS root ");
            queryBuilder.append("INNER JOIN post AS child ON child.mpath LIKE CONCAT(root.mpath, \'%\')");
            queryBuilder.append(" ORDER BY root.mpath ");
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
            queryBuilder.append(", child.mpath ASC ");
            if (limit != null && Objects.equals(sort, "tree")) {
                queryBuilder.append("LIMIT ?");
            }
            queryBuilder.append(';');
        }

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 0;
                preparedStatement.setInt(++parameterIndex, threadId);
                if (since != null) {
                    preparedStatement.setString(++parameterIndex, since);
                }
                if (limit != null) {
                    preparedStatement.setInt(++parameterIndex, limit);
                    if (Objects.equals(sort, "tree")) {
                        preparedStatement.setInt(++parameterIndex, limit);
                    }
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
    public String listUserThreads(String user, String since, Integer limit, String order) {
        JsonArray array = new JsonArray();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM thread ");
        queryBuilder.append("WHERE user = ?");
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
                preparedStatement.setString(++parameterIndex, user);
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
                        object.addProperty("posts", resultSet.getInt("posts"));
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
    public String listForumThreads(String forum, String since, Integer limit, String order) {
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
                        object.addProperty("posts", resultSet.getInt("posts"));
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
    public String remove(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        try (Connection connection = dataSource.getConnection()) {
            String query = "UPDATE thread SET isDeleted = 1, posts = 0 WHERE id = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("thread").getAsInt());
                preparedStatement.execute();
            }

            String updatePostsQuery = "UPDATE post SET isDeleted = 1 WHERE thread = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updatePostsQuery)) {
                preparedStatement.setInt(1, object.get("thread").getAsInt());
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
            int postsCount;
            String updatePostsQuery = "UPDATE post SET isDeleted = 0 WHERE thread = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updatePostsQuery)) {
                preparedStatement.setInt(1, object.get("thread").getAsInt());
                postsCount = preparedStatement.executeUpdate();
            }

            String query = "UPDATE thread SET isDeleted = 0, posts = ? WHERE id = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, postsCount);
                preparedStatement.setInt(2, object.get("thread").getAsInt());
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
            String query = "UPDATE thread SET slug = ?, message = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, object.get("slug").getAsString());
                preparedStatement.setString(2, object.get("message").getAsString());
                preparedStatement.setInt(3, object.get("thread").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details(object.get("thread").getAsInt(), null);
    }

    @Override
    public String vote(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        String likeQuery = "UPDATE thread SET likes = likes + 1 WHERE id = ?";
        String dislikeQuery = "UPDATE thread SET dislikes = dislikes + 1 WHERE id = ?";

        String query = object.get("vote").getAsInt() > 0 ? likeQuery : dislikeQuery;

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("thread").getAsInt());
                preparedStatement.execute();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details(object.get("thread").getAsInt(), null);
    }

    @Override
    public String subscribe(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT INTO subscribe (user, thread) VALUES (?,?);";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, object.get("user").getAsString());
                preparedStatement.setInt(2, object.get("thread").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    @Override
    public String unsubscribe(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        try (Connection connection = dataSource.getConnection()) {
            String query = "DELETE FROM subscribe WHERE user = ? AND thread = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, object.get("user").getAsString());
                preparedStatement.setInt(2, object.get("thread").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    @Override
    public String open(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        try (Connection connection = dataSource.getConnection()) {
            String query = "UPDATE thread SET isClosed = 0 WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("thread").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    @Override
    public String close(String jsonString) {
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();

        try (Connection connection = dataSource.getConnection()) {
            String query = "UPDATE thread SET isClosed = 1 WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("thread").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return object.toString();
    }
}
