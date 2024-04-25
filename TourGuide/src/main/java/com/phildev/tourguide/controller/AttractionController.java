package com.phildev.tourguide.controller;


import com.phildev.tourguide.dto.ClosestAttractionDTO;
import com.phildev.tourguide.service.AttractionService;
import com.phildev.tourguide.service.RewardsService;
import com.phildev.tourguide.service.TripDealService;
import com.phildev.tourguide.service.UserService;
import com.phildev.tourguide.user.User;
import com.phildev.tourguide.user.UserReward;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tripPricer.Provider;

import java.util.List;

@RestController
@Validated
public class AttractionController {

    private final AttractionService attractionService;

    private final UserService userService;

    public AttractionController(AttractionService attractionService, UserService userService){
        this.attractionService = attractionService;
        this.userService = userService;
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
        User user = userService.getUser(userName);
    	VisitedLocation visitedLocation = userService.getUserLocation(user);
    	return attractionService.getFiveClosestAttractionsWithInfos(visitedLocation, user);
    }

   

}