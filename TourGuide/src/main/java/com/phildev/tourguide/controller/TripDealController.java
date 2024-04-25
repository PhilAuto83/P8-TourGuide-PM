package com.phildev.tourguide.controller;

import com.phildev.tourguide.service.TripDealService;
import com.phildev.tourguide.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tripPricer.Provider;

import java.util.List;

@RestController
public class TripDealController {
    private final TripDealService tripDealService;

    private final UserService userService;

    public TripDealController(TripDealService tripDealService, UserService userService){
        this.tripDealService = tripDealService;
        this.userService = userService;
    }


    @GetMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        return tripDealService.getTripDeals(userService.getUser(userName));
    }

}
