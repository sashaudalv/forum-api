package main.database.dao;

import main.models.Response;

/**
 * alex on 03.01.16.
 */
public interface PostDAO {
    int getCount();

    void truncateTable();

    @Deprecated
    int getCount(int threadId);

    Response create(String jsonString);

    Response details(int postId, String[] related);

    Response listForumPosts(String forum, String since, Integer limit, String order);

    Response listThreadPosts(int threadId, String since, Integer limit, String order);

    Response remove(String jsonString);

    Response restore(String jsonString);

    Response update(String jsonString);

    Response vote(String jsonString);
}
