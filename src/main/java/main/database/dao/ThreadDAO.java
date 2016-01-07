package main.database.dao;

/**
 * alex on 03.01.16.
 */
public interface ThreadDAO {
    int getCount();

    void truncateTable();

    String create(String jsonString);

    String details(int threadId, String[] related);

    String listPosts(int threadId, String since, Integer limit, String sort, String order);

    String listUserThreads(String user, String since, Integer limit, String order);

    String listForumThreads(String forum, String since, Integer limit, String order);

    String remove(String jsonString);

    String restore(String jsonString);

    String update(String jsonString);

    String vote(String jsonString);

    String subscribe(String jsonString);

    String unsubscribe(String jsonString);

    String open(String jsonString);

    String close(String jsonString);
}
