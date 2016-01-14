package main.controllers;

import main.database.dao.ForumDAO;
import main.database.dao.impl.ForumDAOImpl;
import main.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * alex on 04.01.16.
 */
@RestController
@RequestMapping(value = "/db/api/forum")
public class ForumController {

    @Autowired
    private DataSource dataSource;

    private ForumDAO forumDAO;

    @PostConstruct
    void init() {
        forumDAO = new ForumDAOImpl(dataSource);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Response create(@RequestBody String body) {
        return forumDAO.create(body);
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public Response details(@RequestParam(value = "forum", required = true) String forum,
                            @RequestParam(value = "related", required = false) String[] related) {
        return forumDAO.details(forum, related);
    }

    @RequestMapping(value = "/listPosts", method = RequestMethod.GET)
    public Response listPosts(@RequestParam(value = "forum", required = true) String forum,
                              @RequestParam(value = "since", required = false) String since,
                              @RequestParam(value = "limit", required = false) Integer limit,
                              @RequestParam(value = "order", required = false) String order,
                              @RequestParam(value = "related", required = false) String[] related) {
        return forumDAO.listPosts(forum, since, limit, order, related);
    }

    @RequestMapping(value = "/listThreads", method = RequestMethod.GET)
    public Response listThreads(@RequestParam(value = "forum", required = true) String forum,
                                @RequestParam(value = "since", required = false) String since,
                                @RequestParam(value = "limit", required = false) Integer limit,
                                @RequestParam(value = "order", required = false) String order,
                                @RequestParam(value = "related", required = false) String[] related) {
        return forumDAO.listThreads(forum, since, limit, order, related);
    }

    @RequestMapping(value = "/listUsers", method = RequestMethod.GET)
    public Response listUsers(@RequestParam(value = "forum", required = true) String forum,
                              @RequestParam(value = "since_id", required = false) Integer sinceId,
                              @RequestParam(value = "limit", required = false) Integer limit,
                              @RequestParam(value = "order", required = false) String order) {
        return forumDAO.listUsers(forum, sinceId, limit, order);
    }
}
