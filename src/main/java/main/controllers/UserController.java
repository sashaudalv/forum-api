package main.controllers;

import main.database.dao.UserDAO;
import main.database.dao.impl.UserDAOImpl;
import main.database.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    public UserData create(@RequestBody String body){
        return userDAO.create(body);
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public void details(){

    }

    @RequestMapping(value = "/follow", method = RequestMethod.POST  )
    public void follow(){

    }

    @RequestMapping(value = "/listFollowers", method = RequestMethod.GET)
    public void listFollowers(){

    }

    @RequestMapping(value = "/listFollowing", method = RequestMethod.GET)
    public void listFollowing(){

    }

    @RequestMapping(value = "/listPosts", method = RequestMethod.GET)
    public void listPosts(){

    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    public void unfollow(){

    }

    @RequestMapping(value = "/updateProfile", method = RequestMethod.POST)
    public void updateProfile(){

    }
}
