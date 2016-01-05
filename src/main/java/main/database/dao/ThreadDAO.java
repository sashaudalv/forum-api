package main.database.dao;

/**
 * alex on 03.01.16.
 */
public interface ThreadDAO {
    int getCount();
    void truncateTable();

    String details(int threadId, String[] related);

    String listPosts(int threadId, String since, Integer limit, String sort, String order);
}
