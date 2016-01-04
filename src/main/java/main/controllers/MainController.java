package main.controllers;

import main.database.dao.ForumDAO;
import main.database.dao.PostDAO;
import main.database.dao.ThreadDAO;
import main.database.dao.UserDAO;
import main.database.dao.impl.ForumDAOImpl;
import main.database.dao.impl.PostDAOImpl;
import main.database.dao.impl.ThreadDAOImpl;
import main.database.dao.impl.UserDAOImpl;
import main.models.SimpleMapResponse;
import main.models.SimpleStringResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * alex on 03.01.16.
 */
@RestController
@RequestMapping(value = "/db/api")
public class MainController {

    @Autowired
    private Connection connection;

    private UserDAO userDAO;
    private ForumDAO forumDAO;
    private ThreadDAO threadDAO;
    private PostDAO postDAO;

    @PostConstruct
    void init() {
        userDAO = new UserDAOImpl(connection);
        forumDAO = new ForumDAOImpl(connection);
        threadDAO = new ThreadDAOImpl(connection);
        postDAO = new PostDAOImpl(connection);
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public SimpleMapResponse status() {
        Map<String, Integer> response = new HashMap<>();
        response.put("user",userDAO.getCount());
        response.put("thread", threadDAO.getCount());
        response.put("forum", forumDAO.getCount());
        response.put("post", postDAO.getCount());
        return new SimpleMapResponse(response);
    }

    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public SimpleStringResponse clear() {
        userDAO.truncateTable();
        forumDAO.truncateTable();
        threadDAO.truncateTable();
        postDAO.truncateTable();
        return new SimpleStringResponse("\"OK\"");
    }
}
