package main.database.dao;

/**
 * alex on 03.01.16.
 */
public interface ForumDAO {
    int getCount();

    void truncateTable();

    String create(String jsonString);

    String details(String forum, String[] related);

    String listPosts(String forum, String since, Integer limit, String order, String[] related);

    String listThreads(String forum, String since, Integer limit, String order, String[] related);

    String listUsers(String forum, Integer sinceId, Integer limit, String order);
}
