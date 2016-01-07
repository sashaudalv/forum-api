package main.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import main.database.dao.UserDAO;
import main.database.dao.impl.UserDAOImpl;
import main.models.SimpleStringResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.sql.Connection;

/**
 * alex on 03.01.16.
 */
@RestController
@RequestMapping(value = "/db/api/user")
public class UserController {

    @Autowired
    private Connection connection;

    private UserDAO userDAO;

    @PostConstruct
    void init() {
        userDAO = new UserDAOImpl(connection);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public SimpleStringResponse create(@RequestBody String body){
        String ret = userDAO.create(body);
        if (ret == null) {
            return new SimpleStringResponse(5);
        } else {
            return new SimpleStringResponse(ret);

        }
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public SimpleStringResponse details(@RequestParam(value = "user") String email){
        return new SimpleStringResponse(userDAO.details(email));
    }

    @RequestMapping(value = "/follow", method = RequestMethod.POST  )
    public SimpleStringResponse follow(@RequestBody String body){
        JsonObject object = new JsonParser().parse(body).getAsJsonObject();
        String follower = object.get("follower").getAsString();
        String followee = object.get("followee").getAsString();
        userDAO.follow(follower, followee);
        return new SimpleStringResponse(userDAO.details(follower));
    }

    @RequestMapping(value = "/listFollowers", method = RequestMethod.GET)
    public SimpleStringResponse listFollowers(@RequestParam(value = "user", required = true) String email,
                              @RequestParam(value = "limit", required = false) Integer limit,
                              @RequestParam(value = "order", required = false) String order,
                              @RequestParam(value = "since_id", required = false) Integer sinceId){
        return new SimpleStringResponse(userDAO.listFollowers(email, limit, order, sinceId));
    }

    @RequestMapping(value = "/listFollowing", method = RequestMethod.GET)
    public SimpleStringResponse listFollowing(@RequestParam(value = "user", required = true) String email,
                              @RequestParam(value = "limit", required = false) Integer limit,
                              @RequestParam(value = "order", required = false) String order,
                              @RequestParam(value = "since_id", required = false) Integer sinceId){
        return new SimpleStringResponse(userDAO.listFollowing(email, limit, order, sinceId));
    }

    @RequestMapping(value = "/listPosts", method = RequestMethod.GET)
    public SimpleStringResponse listPosts(@RequestParam(value = "user", required = true) String email,
                                          @RequestParam(value = "limit", required = false) Integer limit,
                                          @RequestParam(value = "order", required = false) String order,
                                          @RequestParam(value = "since", required = false) String since){
        return new SimpleStringResponse(userDAO.listPosts(email, limit, order, since));
    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    public SimpleStringResponse unfollow(@RequestBody String body){
        JsonObject object = new JsonParser().parse(body).getAsJsonObject();
        String follower = object.get("follower").getAsString();
        String followee = object.get("followee").getAsString();
        userDAO.unfollow(follower, followee);
        return new SimpleStringResponse(userDAO.details(follower));
    }

    @RequestMapping(value = "/updateProfile", method = RequestMethod.POST)
    public SimpleStringResponse updateProfile(@RequestBody String body){
        userDAO.updateProfile(body);
        JsonObject object = new JsonParser().parse(body).getAsJsonObject();
        return new SimpleStringResponse(userDAO.details(object.get("user").getAsString()));
    }
}
