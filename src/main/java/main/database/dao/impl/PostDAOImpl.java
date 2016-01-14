package main.database.dao.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import main.database.dao.PostDAO;
import main.database.executor.TExecutor;
import main.models.PostModel;
import main.models.Response;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;

/**
 * alex on 03.01.16.
 */
public class PostDAOImpl implements PostDAO {

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
    public Response create(String jsonString) {
        JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        PostModel post;
        try {
            post = new PostModel(object);
        } catch (Exception e) {
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT INTO post (date, thread, message, user, forum, parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, post.getDate());
                preparedStatement.setInt(2, (Integer) post.getThread());
                preparedStatement.setString(3, post.getMessage());
                preparedStatement.setString(4, (String) post.getUser());
                preparedStatement.setString(5, (String) post.getForum());
                preparedStatement.setObject(6, post.getParent());
                preparedStatement.setBoolean(7, post.getIsApproved());
                preparedStatement.setBoolean(8, post.getIsHighlighted());
                preparedStatement.setBoolean(9, post.getIsEdited());
                preparedStatement.setBoolean(10, post.getIsSpam());
                preparedStatement.setBoolean(11, post.getIsDeleted());
                preparedStatement.execute();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        post.setId(resultSet.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.UNKNOWN_ERROR);
        }

        return new Response(post);
    }

    @Override
    public Response details(int postId, String[] related) {
        PostModel postModel;

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM post WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, postId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        postModel = new PostModel(resultSet);
                    } else {
                        return new Response(Response.Codes.NOT_FOUND);
                    }
                }
            }
            if (related != null) {
                if (Arrays.asList(related).contains("user")) {
                    postModel.setUser(new UserDAOImpl(dataSource).details((String) postModel.getUser()).getResponse());
                }
                if (Arrays.asList(related).contains("forum")) {
                    postModel.setForum(new ForumDAOImpl(dataSource).details((String) postModel.getForum(), null).getResponse());
                }
                if (Arrays.asList(related).contains("thread")) {
                    postModel.setThread(new ThreadDAOImpl(dataSource).details((Integer) postModel.getThread(), null).getResponse());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.UNKNOWN_ERROR);
        }

        return new Response(postModel);
    }

    @Override
    public Response listForumPosts(String forum, String since, Integer limit, String order) {
        return new ForumDAOImpl(dataSource).listPosts(forum, since, limit, order, null);
    }

    @Override
    public Response listThreadPosts(int threadId, String since, Integer limit, String order) {
        return new ThreadDAOImpl(dataSource).listPosts(threadId, since, limit, null, order);
    }

    @Override
    public Response remove(String jsonString) {
        JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "UPDATE post SET isDeleted = 1 WHERE id = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return new Response(new Gson().fromJson(object, Object.class));
    }

    @Override
    public Response restore(String jsonString) {
        JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "UPDATE post SET isDeleted = 0 WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return new Response(new Gson().fromJson(object, Object.class));
    }

    @Override
    public Response update(String jsonString) {
        JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "UPDATE post SET message = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, object.get("message").getAsString());
                preparedStatement.setInt(2, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return details(object.get("post").getAsInt(), null);
    }

    @Override
    public Response vote(String jsonString) {
        JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        String likeQuery = "UPDATE post SET likes = likes + 1 WHERE id = ?";
        String dislikeQuery = "UPDATE post SET dislikes = dislikes + 1 WHERE id = ?";

        String query = object.get("vote").getAsInt() > 0 ? likeQuery : dislikeQuery;

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return details(object.get("post").getAsInt(), null);
    }
}
