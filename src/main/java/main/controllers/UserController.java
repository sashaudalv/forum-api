package main.controllers;

import main.database.dao.UserDAO;
import main.database.dao.impl.UserDAOImpl;
import main.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * alex on 03.01.16.
 */
@RestController
@RequestMapping(value = "/db/api/user")
public class UserController {

    @Autowired
    private DataSource dataSource;

    private UserDAO userDAO;

    @PostConstruct
    void init() {
        userDAO = new UserDAOImpl(dataSource);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Response create(@RequestBody String body) {
        return userDAO.create(body);
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public Response details(@RequestParam(value = "user") String email) {
        return userDAO.details(email);
    }

    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    public Response follow(@RequestBody String body) {
        return userDAO.follow(body);
    }

    @RequestMapping(value = "/listFollowers", method = RequestMethod.GET)
    public Response listFollowers(@RequestParam(value = "user", required = true) String email,
                                  @RequestParam(value = "limit", required = false) Integer limit,
                                  @RequestParam(value = "order", required = false) String order,
                                  @RequestParam(value = "since_id", required = false) Integer sinceId) {
        return userDAO.listFollowers(email, limit, order, sinceId);
    }

    @RequestMapping(value = "/listFollowing", method = RequestMethod.GET)
    public Response listFollowing(@RequestParam(value = "user", required = true) String email,
                                  @RequestParam(value = "limit", required = false) Integer limit,
                                  @RequestParam(value = "order", required = false) String order,
                                  @RequestParam(value = "since_id", required = false) Integer sinceId) {
        return userDAO.listFollowing(email, limit, order, sinceId);
    }

    @RequestMapping(value = "/listPosts", method = RequestMethod.GET)
    public Response listPosts(@RequestParam(value = "user", required = true) String email,
                              @RequestParam(value = "limit", required = false) Integer limit,
                              @RequestParam(value = "order", required = false) String order,
                              @RequestParam(value = "since", required = false) String since) {
        return userDAO.listPosts(email, limit, order, since);
    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    public Response unfollow(@RequestBody String body) {
        return userDAO.unfollow(body);
    }

    @RequestMapping(value = "/updateProfile", method = RequestMethod.POST)
    public Response updateProfile(@RequestBody String body) {
        return userDAO.updateProfile(body);
    }
}
