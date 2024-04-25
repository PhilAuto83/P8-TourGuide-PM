package com.phildev.tourguide.controller;


import com.phildev.tourguide.service.RewardsService;
import com.phildev.tourguide.service.UserService;
import com.phildev.tourguide.user.UserReward;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RewardController {

    private final RewardsService rewardsService;
    private final UserService userService;


    public RewardController(RewardsService rewardsService, UserService userService){
        this.rewardsService = rewardsService;
        this.userService = userService;

    }

    @GetMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        return rewardsService.getUserRewards(userService.getUser(userName));
    }

}
