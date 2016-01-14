package main.controllers;

import main.database.dao.PostDAO;
import main.database.dao.impl.PostDAOImpl;
import main.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * alex on 04.01.16.
 */
@RestController
@RequestMapping(value = "/db/api/post")
public class PostController {

    @Autowired
    private DataSource dataSource;

    private PostDAO postDAO;

    @PostConstruct
    void init() {
        postDAO = new PostDAOImpl(dataSource);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Response create(@RequestBody String body) {
        return postDAO.create(body);
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public Response details(@RequestParam(value = "post", required = true) int postId,
                            @RequestParam(value = "related", required = false) String[] related) {
        return postDAO.details((int) postId, related);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, params = {"forum"})
    public Response listForumPosts(@RequestParam(value = "forum", required = true) String forum,
                                   @RequestParam(value = "since", required = false) String since,
                                   @RequestParam(value = "limit", required = false) Integer limit,
                                   @RequestParam(value = "order", required = false) String order) {
        return postDAO.listForumPosts(forum, since, limit, order);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, params = {"thread"})
    public Response listThreadPosts(@RequestParam(value = "thread", required = true) int threadId,
                                    @RequestParam(value = "since", required = false) String since,
                                    @RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "order", required = false) String order) {
        return postDAO.listThreadPosts((int) threadId, since, limit, order);
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public Response remove(@RequestBody String body) {
        return postDAO.remove(body);
    }

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    public Response restore(@RequestBody String body) {
        return postDAO.restore(body);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Response update(@RequestBody String body) {
        return postDAO.update(body);
    }

    @RequestMapping(value = "/vote", method = RequestMethod.POST)
    public Response vote(@RequestBody String body) {
        return postDAO.vote(body);
    }
}
