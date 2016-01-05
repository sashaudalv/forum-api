package main.controllers;

import main.database.dao.PostDAO;
import main.database.dao.impl.PostDAOImpl;
import main.models.SimpleStringResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.sql.Connection;

/**
 * alex on 04.01.16.
 */
@RestController
@RequestMapping(value = "/db/api/post")
public class PostController {

    @Autowired
    private Connection connection;

    private PostDAO postDAO;

    @PostConstruct
    void init() {
        postDAO = new PostDAOImpl(connection);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public SimpleStringResponse create(@RequestBody String body){
        return new SimpleStringResponse(postDAO.create(body));
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public SimpleStringResponse details(@RequestParam(value = "post", required = true) int postId,
                                        @RequestParam(value = "related", required = false) String[] related){
        return new SimpleStringResponse(postDAO.details(postId, related));
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, params={"forum"})
    public SimpleStringResponse listForumPosts(@RequestParam(value = "forum", required = true) String forum,
                                          @RequestParam(value = "since", required = false) String since,
                                          @RequestParam(value = "limit", required = false) Integer limit,
                                          @RequestParam(value = "order", required = false) String order){
        return new SimpleStringResponse(postDAO.listForumPosts(forum, since, limit, order));
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, params={"thread"})
    public SimpleStringResponse listThreadPosts(@RequestParam(value = "thread", required = true) int threadId,
                                          @RequestParam(value = "since", required = false) String since,
                                          @RequestParam(value = "limit", required = false) Integer limit,
                                          @RequestParam(value = "order", required = false) String order){
        return new SimpleStringResponse(postDAO.listThreadPosts(threadId, since, limit, order));
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public SimpleStringResponse remove(@RequestBody String body){
        return new SimpleStringResponse(postDAO.remove(body));
    }

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    public SimpleStringResponse restore(@RequestBody String body){
        return new SimpleStringResponse(postDAO.restore(body));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public SimpleStringResponse update(@RequestBody String body){
        return new SimpleStringResponse(postDAO.update(body));
    }

    @RequestMapping(value = "/vote", method = RequestMethod.POST)
    public SimpleStringResponse vote(@RequestBody String body){
        return new SimpleStringResponse(postDAO.vote(body));
    }
}
