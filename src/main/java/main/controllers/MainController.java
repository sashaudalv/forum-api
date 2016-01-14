package main.controllers;

import main.database.dao.ForumDAO;
import main.database.dao.PostDAO;
import main.database.dao.ThreadDAO;
import main.database.dao.UserDAO;
import main.database.dao.impl.ForumDAOImpl;
import main.database.dao.impl.PostDAOImpl;
import main.database.dao.impl.ThreadDAOImpl;
import main.database.dao.impl.UserDAOImpl;
import main.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * alex on 03.01.16.
 */
@RestController
@RequestMapping(value = "/db/api")
public class MainController {

    @Autowired
    private DataSource dataSource;

    private UserDAO userDAO;
    private ForumDAO forumDAO;
    private ThreadDAO threadDAO;
    private PostDAO postDAO;

    @PostConstruct
    void init() {
        userDAO = new UserDAOImpl(dataSource);
        forumDAO = new ForumDAOImpl(dataSource);
        threadDAO = new ThreadDAOImpl(dataSource);
        postDAO = new PostDAOImpl(dataSource);
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public Response status() {
        Map<String, Integer> response = new HashMap<>();
        response.put("user", userDAO.getCount());
        response.put("thread", threadDAO.getCount());
        response.put("forum", forumDAO.getCount());
        response.put("post", postDAO.getCount());
        return new Response(response);
    }

    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public Response clear() {
        userDAO.truncateTable();
        forumDAO.truncateTable();
        threadDAO.truncateTable();
        postDAO.truncateTable();
        return new Response(Response.Codes.OK);
    }
}
