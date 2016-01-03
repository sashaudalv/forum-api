package main.database.dao;

import main.database.data.UserData;

import java.util.Collection;
import java.util.Map;

/**
 * alex on 03.01.16.
 */
public interface UserDAO {
    int getCount();
    void truncateTable();
    UserData create(String jsonString);
    Map<String, Object> details();
    void follow();
    void unfollow();
    Collection<Object> listFollowers();
    Collection<Object> listFollowing();
    Collection<Object> listPosts();
    void updateProfile();
}
