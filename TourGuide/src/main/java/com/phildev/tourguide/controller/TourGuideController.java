package com.phildev.tourguide.controller;


import com.phildev.tourguide.dto.ClosestAttractionDTO;
import com.phildev.tourguide.service.TourGuideService;
import com.phildev.tourguide.user.User;

import com.phildev.tourguide.user.UserReward;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tripPricer.Provider;

import java.util.List;

@RestController
@Validated
public class TourGuideController {

	@Autowired
    TourGuideService tourGuideService;
	
    @GetMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @PostMapping(value="/createUser", consumes="application/json", produces = "application/json")
    public ResponseEntity<String> createUser(@Valid @RequestBody User user, BindingResult result){
        tourGuideService.addUser(user);
        return new ResponseEntity<>(String.format("User %s was created with success with id %s", user.getUserName(), user.getUserId()), HttpStatus.CREATED);
    }
    
    @GetMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam @NotBlank(message = "Username cannot be null or empty") String userName) {
    	return tourGuideService.getUserLocation(getUser(userName));
    }

    /**
     * This method retrieves a {@link User} by his or her username and gets his or her location to show the five closest attractions with infos such as
     * attraction name, latituden longitude, distance from user location and reward points
     * @param userName
     * @return ClosestAttractionDto which contains an Object {@link gpsUtil.location.Location} which is the user location
     * and a List<Map>String, Object>> representing the info about each attraction
     */
    @GetMapping("/getNearbyAttractions")
    public ClosestAttractionDTO getNearbyAttractions(@RequestParam String userName) {
        User user = tourGuideService.getUser(userName);
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    	return tourGuideService.getFiveClosestAttractionsWithInfos(visitedLocation, user);
    }
    
    @GetMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(getUser(userName));
    }
       
    @GetMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }
    
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   

}