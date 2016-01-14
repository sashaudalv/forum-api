package main.models;

import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * alex on 14.01.16.
 */
public class ThreadModel {

    private int id;
    private Object forum;
    private final String message;
    private final String slug;
    private final String title;
    private Object user;
    private String date;
    private final int dislikes;
    private final int likes;
    private final int points;
    private final int posts;
    private final boolean isClosed;
    private final boolean isDeleted;

    public ThreadModel(int id, String forum, String message, String slug, String title, Object user, String date,
                       int dislikes, int likes, int points, int posts, boolean isClosed, boolean isDeleted) {
        this.id = id;
        this.forum = forum;
        this.message = message;
        this.slug = slug;
        this.title = title;
        this.user = user;
        this.date = date;
        this.dislikes = dislikes;
        this.likes = likes;
        this.points = points;
        this.posts = posts;
        this.isClosed = isClosed;
        this.isDeleted = isDeleted;
    }

    public ThreadModel(String forum, String message, String slug, String title, Object user, String date,
                       boolean isClosed, boolean isDeleted) {
        this(-1, forum, message, slug, title, user, date, 0, 0, 0, 0, isClosed, isDeleted);
    }

    public ThreadModel(JsonObject jsonObject) throws Exception {
        this(
                jsonObject.get("forum").getAsString(),
                jsonObject.get("message").getAsString(),
                jsonObject.get("slug").getAsString(),
                jsonObject.get("title").getAsString(),
                jsonObject.get("user").getAsString(),
                jsonObject.get("date").getAsString(),
                jsonObject.get("isClosed").getAsBoolean(),
                jsonObject.has("isDeleted") && jsonObject.get("isDeleted").getAsBoolean()
        );
    }

    public ThreadModel(ResultSet resultSet) throws SQLException {
        this(
                resultSet.getInt("id"),
                resultSet.getString("forum"),
                resultSet.getString("message"),
                resultSet.getString("slug"),
                resultSet.getString("title"),
                resultSet.getString("user"),
                resultSet.getString("date"),
                resultSet.getInt("dislikes"),
                resultSet.getInt("likes"),
                resultSet.getInt("likes") - resultSet.getInt("dislikes"),
                resultSet.getInt("posts"),
                resultSet.getBoolean("isClosed"),
                resultSet.getBoolean("isDeleted")
        );
        date = date.substring(0, date.length() - 2);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getForum() {
        return forum;
    }

    public void setForum(Object forumObject) {
        forum = forumObject;
    }

    public String getMessage() {
        return message;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object userObject) {
        user = userObject;
    }

    public String getDate() {
        return date;
    }

    public int getDislikes() {
        return dislikes;
    }

    public int getLikes() {
        return likes;
    }

    public int getPoints() {
        return points;
    }

    public int getPosts() {
        return posts;
    }

    public boolean getIsClosed() {
        return isClosed;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }
}
