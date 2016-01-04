package main.database.dao;

/**
 * alex on 03.01.16.
 */
public interface UserDAO {
    int getCount();
    void truncateTable();
    String create(String jsonString);
    String details(String email);
    void follow(String follower, String followee);
    void unfollow(String follower, String followee);
    String listFollowers(String email, Integer limit, String order, Integer sinceId);
    String listFollowing(String email, Integer limit, String order, Integer sinceId);
    String listPosts(String email, Integer limit, String order, String since);
    void updateProfile(String jsonString);
}
