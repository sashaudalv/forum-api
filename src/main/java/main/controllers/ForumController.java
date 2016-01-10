package main.controllers;

import main.database.dao.ForumDAO;
import main.database.dao.impl.ForumDAOImpl;
import main.models.SimpleStringResponse;
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
    public SimpleStringResponse create(@RequestBody String body){
        return new SimpleStringResponse(forumDAO.create(body));
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public SimpleStringResponse details(@RequestParam(value = "forum", required = true) String forum,
                                        @RequestParam(value = "related", required = false) String[] related){
        return new SimpleStringResponse(forumDAO.details(forum, related));
    }

    @RequestMapping(value = "/listPosts", method = RequestMethod.GET)
    public SimpleStringResponse listPosts(@RequestParam(value = "forum", required = true) String forum,
                                          @RequestParam(value = "since", required = false) String since,
                                          @RequestParam(value = "limit", required = false) Integer limit,
                                          @RequestParam(value = "order", required = false) String order,
                                          @RequestParam(value = "related", required = false) String[] related){
        return new SimpleStringResponse(forumDAO.listPosts(forum, since, limit, order, related));
    }

    @RequestMapping(value = "/listThreads", method = RequestMethod.GET)
    public SimpleStringResponse listThreads(@RequestParam(value = "forum", required = true) String forum,
                                            @RequestParam(value = "since", required = false) String since,
                                            @RequestParam(value = "limit", required = false) Integer limit,
                                            @RequestParam(value = "order", required = false) String order,
                                            @RequestParam(value = "related", required = false) String[] related){
        return new SimpleStringResponse(forumDAO.listThreads(forum, since, limit, order, related));
    }

    @RequestMapping(value = "/listUsers", method = RequestMethod.GET)
    public SimpleStringResponse listUsers(@RequestParam(value = "forum", required = true) String forum,
                                          @RequestParam(value = "since_id", required = false) Integer sinceId,
                                          @RequestParam(value = "limit", required = false) Integer limit,
                                          @RequestParam(value = "order", required = false) String order){
        return new SimpleStringResponse(forumDAO.listUsers(forum, sinceId, limit, order));
    }
}
