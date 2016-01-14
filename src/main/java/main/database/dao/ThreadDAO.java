package main.database.dao;

import main.models.Response;

/**
 * alex on 03.01.16.
 */
public interface ThreadDAO {
    int getCount();

    void truncateTable();

    Response create(String jsonString);

    Response details(int threadId, String[] related);

    Response listPosts(int threadId, String since, Integer limit, String sort, String order);

    Response listUserThreads(String user, String since, Integer limit, String order);

    Response listForumThreads(String forum, String since, Integer limit, String order);

    Response remove(String jsonString);

    Response restore(String jsonString);

    Response update(String jsonString);

    Response vote(String jsonString);

    Response subscribe(String jsonString);

    Response unsubscribe(String jsonString);

    Response open(String jsonString);

    Response close(String jsonString);
}
