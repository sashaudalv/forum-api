package main.database.dao;

import main.models.Response;

/**
 * alex on 03.01.16.
 */
public interface UserDAO {
    int getCount();

    void truncateTable();

    Response create(String jsonString);

    Response details(String email);

    Response follow(String jsonString);

    Response unfollow(String jsonString);

    Response listFollowers(String email, Integer limit, String order, Integer sinceId);

    Response listFollowing(String email, Integer limit, String order, Integer sinceId);

    Response listPosts(String email, Integer limit, String order, String since);

    Response updateProfile(String jsonString);
}
