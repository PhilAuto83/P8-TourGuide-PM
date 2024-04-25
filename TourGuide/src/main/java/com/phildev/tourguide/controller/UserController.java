package com.phildev.tourguide.controller;

import com.phildev.tourguide.service.UserService;
import com.phildev.tourguide.user.User;
import gpsUtil.location.VisitedLocation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private  final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping(value="/createUser", consumes="application/json", produces = "application/json")
    public ResponseEntity<String> createUser(@Valid @RequestBody User user, BindingResult result){
        userService.addUser(user);
        return new ResponseEntity<>(String.format("User %s was created with success with id %s", user.getUserName(), user.getUserId()), HttpStatus.CREATED);
    }

    @GetMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam @NotBlank(message = "Username cannot be null or empty") String userName) {
        return userService.getUserLocation(userService.getUser(userName));
    }



}
