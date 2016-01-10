package main.controllers;

import main.database.dao.ThreadDAO;
import main.database.dao.impl.ThreadDAOImpl;
import main.models.SimpleStringResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Arrays;

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
    public SimpleStringResponse create(@RequestBody String body) {
        return new SimpleStringResponse(threadDAO.create(body));
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public SimpleStringResponse details(@RequestParam(value = "thread", required = true) int threadId,
                                        @RequestParam(value = "related", required = false) String[] related) {
        if (threadId < 1) {
            return new SimpleStringResponse(1);
        }
        if (related != null && Arrays.asList(related).contains("thread")) {
            return new SimpleStringResponse(3);
        }
        return new SimpleStringResponse(threadDAO.details(threadId, related));
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, params = {"user"})
    public SimpleStringResponse listUserThreads(@RequestParam(value = "user", required = true) String user,
                                                @RequestParam(value = "since", required = false) String since,
                                                @RequestParam(value = "limit", required = false) Integer limit,
                                                @RequestParam(value = "order", required = false) String order) {
        return new SimpleStringResponse(threadDAO.listUserThreads(user, since, limit, order));
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, params = {"forum"})
    public SimpleStringResponse listForumThreads(@RequestParam(value = "forum", required = true) String forum,
                                                 @RequestParam(value = "since", required = false) String since,
                                                 @RequestParam(value = "limit", required = false) Integer limit,
                                                 @RequestParam(value = "order", required = false) String order) {
        return new SimpleStringResponse(threadDAO.listForumThreads(forum, since, limit, order));
    }

    @RequestMapping(value = "/listPosts", method = RequestMethod.GET)
    public SimpleStringResponse listPosts(@RequestParam(value = "thread", required = true) int threadId,
                                          @RequestParam(value = "since", required = false) String since,
                                          @RequestParam(value = "limit", required = false) Integer limit,
                                          @RequestParam(value = "sort", required = false) String sort,
                                          @RequestParam(value = "order", required = false) String order) {
        return new SimpleStringResponse(threadDAO.listPosts(threadId, since, limit, sort, order));
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public SimpleStringResponse remove(@RequestBody String body) {
        return new SimpleStringResponse(threadDAO.remove(body));
    }

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    public SimpleStringResponse restore(@RequestBody String body) {
        return new SimpleStringResponse(threadDAO.restore(body));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public SimpleStringResponse update(@RequestBody String body) {
        return new SimpleStringResponse(threadDAO.update(body));
    }

    @RequestMapping(value = "/vote", method = RequestMethod.POST)
    public SimpleStringResponse vote(@RequestBody String body) {
        return new SimpleStringResponse(threadDAO.vote(body));
    }

    @RequestMapping(value = "/subscribe", method = RequestMethod.POST)
    public SimpleStringResponse subscribe(@RequestBody String body) {
        return new SimpleStringResponse(threadDAO.subscribe(body));
    }

    @RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
    public SimpleStringResponse unsubscribe(@RequestBody String body) {
        return new SimpleStringResponse(threadDAO.unsubscribe(body));
    }

    @RequestMapping(value = "/open", method = RequestMethod.POST)
    public SimpleStringResponse open(@RequestBody String body) {
        return new SimpleStringResponse(threadDAO.open(body));
    }

    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public SimpleStringResponse close(@RequestBody String body) {
        return new SimpleStringResponse(threadDAO.close(body));
    }
}
