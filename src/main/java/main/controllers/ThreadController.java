package main.controllers;

import main.database.dao.ThreadDAO;
import main.database.dao.impl.ThreadDAOImpl;
import main.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * alex on 05.01.16.
 */
@RestController
@RequestMapping(value = "/db/api/thread")
public class ThreadController {

    @Autowired
    private DataSource dataSource;

    private ThreadDAO threadDAO;

    @PostConstruct
    void init() {
        threadDAO = new ThreadDAOImpl(dataSource);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Response create(@RequestBody String body) {
        return threadDAO.create(body);
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public Response details(@RequestParam(value = "thread", required = true) int threadId,
                            @RequestParam(value = "related", required = false) String[] related) {
        return threadDAO.details((int) threadId, related);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, params = {"user"})
    public Response listUserThreads(@RequestParam(value = "user", required = true) String user,
                                    @RequestParam(value = "since", required = false) String since,
                                    @RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "order", required = false) String order) {
        return threadDAO.listUserThreads(user, since, limit, order);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, params = {"forum"})
    public Response listForumThreads(@RequestParam(value = "forum", required = true) String forum,
                                     @RequestParam(value = "since", required = false) String since,
                                     @RequestParam(value = "limit", required = false) Integer limit,
                                     @RequestParam(value = "order", required = false) String order) {
        return threadDAO.listForumThreads(forum, since, limit, order);
    }

    @RequestMapping(value = "/listPosts", method = RequestMethod.GET)
    public Response listPosts(@RequestParam(value = "thread", required = true) int threadId,
                              @RequestParam(value = "since", required = false) String since,
                              @RequestParam(value = "limit", required = false) Integer limit,
                              @RequestParam(value = "sort", required = false) String sort,
                              @RequestParam(value = "order", required = false) String order) {
        return threadDAO.listPosts((int) threadId, since, limit, sort, order);
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public Response remove(@RequestBody String body) {
        return threadDAO.remove(body);
    }

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    public Response restore(@RequestBody String body) {
        return threadDAO.restore(body);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Response update(@RequestBody String body) {
        return threadDAO.update(body);
    }

    @RequestMapping(value = "/vote", method = RequestMethod.POST)
    public Response vote(@RequestBody String body) {
        return threadDAO.vote(body);
    }

    @RequestMapping(value = "/subscribe", method = RequestMethod.POST)
    public Response subscribe(@RequestBody String body) {
        return threadDAO.subscribe(body);
    }

    @RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
    public Response unsubscribe(@RequestBody String body) {
        return threadDAO.unsubscribe(body);
    }

    @RequestMapping(value = "/open", method = RequestMethod.POST)
    public Response open(@RequestBody String body) {
        return threadDAO.open(body);
    }

    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public Response close(@RequestBody String body) {
        return threadDAO.close(body);
    }
}
