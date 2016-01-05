package main.database.dao;

/**
 * alex on 03.01.16.
 */
public interface PostDAO {
    int getCount();

    void truncateTable();

    int getCount(int threadId);

    String create(String jsonString);

    String details(int postId, String[] related);

    String listForumPosts(String forum, String since, Integer limit, String order);

    String listThreadPosts(int threadId, String since, Integer limit, String order);

    String remove(String jsonString);

    String restore(String jsonString);

    String update(String jsonString);

    String vote(String jsonString);
}
