package main.models;

import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * alex on 14.01.16.
 */
public class PostModel {

    private int id;
    private Object forum;
    private Object thread;
    private final String message;
    private Object user;
    private String date;
    private final Integer parent;
    private final int dislikes;
    private final int likes;
    private final int points;
    private final boolean isApproved;
    private final boolean isDeleted;
    private final boolean isEdited;
    private final boolean isHighlighted;
    private final boolean isSpam;

    public PostModel(int id, Object forum, Object thread, String message, Object user, String date,
                     Integer parent, int dislikes, int likes, int points, boolean isApproved, boolean isDeleted,
                     boolean isEdited, boolean isHighlighted, boolean isSpam) {
        this.id = id;
        this.forum = forum;
        this.thread = thread;
        this.message = message;
        this.user = user;
        this.date = date;
        this.parent = parent;
        this.dislikes = dislikes;
        this.likes = likes;
        this.points = points;
        this.isApproved = isApproved;
        this.isDeleted = isDeleted;
        this.isEdited = isEdited;
        this.isHighlighted = isHighlighted;
        this.isSpam = isSpam;
    }

    public PostModel(Object forum, Object thread, String message, Object user, String date,
                     Integer parent, boolean isApproved, boolean isDeleted, boolean isEdited,
                     boolean isHighlighted, boolean isSpam) {
        this(-1, forum, thread, message, user, date, parent, 0, 0, 0, isApproved, isDeleted, isEdited, isHighlighted, isSpam);
    }

    public PostModel(JsonObject jsonObject) throws Exception {
        this(
                jsonObject.get("forum").getAsString(),
                jsonObject.get("thread").getAsInt(),
                jsonObject.get("message").getAsString(),
                jsonObject.get("user").getAsString(),
                jsonObject.get("date").getAsString(),
                !jsonObject.has("parent") || jsonObject.get("parent").isJsonNull() ? null : jsonObject.get("parent").getAsInt(),
                jsonObject.has("isApproved") && jsonObject.get("isApproved").getAsBoolean(),
                jsonObject.has("isDeleted") && jsonObject.get("isDeleted").getAsBoolean(),
                jsonObject.has("isEdited") && jsonObject.get("isEdited").getAsBoolean(),
                jsonObject.has("isHighlighted") && jsonObject.get("isHighlighted").getAsBoolean(),
                jsonObject.has("isSpam") && jsonObject.get("isSpam").getAsBoolean()
        );
    }

    public PostModel(ResultSet resultSet) throws SQLException {
        this(
                resultSet.getInt("id"),
                resultSet.getString("forum"),
                resultSet.getInt("thread"),
                resultSet.getString("message"),
                resultSet.getString("user"),
                resultSet.getString("date"),
                (Integer) resultSet.getObject("parent"),
                resultSet.getInt("dislikes"),
                resultSet.getInt("likes"),
                resultSet.getInt("likes") - resultSet.getInt("dislikes"),
                resultSet.getBoolean("isApproved"),
                resultSet.getBoolean("isDeleted"),
                resultSet.getBoolean("isEdited"),
                resultSet.getBoolean("isHighlighted"),
                resultSet.getBoolean("isSpam")
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

    public void setForum(Object forum) {
        this.forum = forum;
    }

    public Object getThread() {
        return thread;
    }

    public void setThread(Object thread) {
        this.thread = thread;
    }

    public String getMessage() {
        return message;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public Integer getParent() {
        return parent;
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

    public boolean getIsApproved() {
        return isApproved;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public boolean getIsEdited() {
        return isEdited;
    }

    public boolean getIsHighlighted() {
        return isHighlighted;
    }

    public boolean getIsSpam() {
        return isSpam;
    }
}
