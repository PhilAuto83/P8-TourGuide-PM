package com.phildev.tourguide;

import com.phildev.tourguide.helper.InternalTestHelper;
import com.phildev.tourguide.service.RewardsService;
import com.phildev.tourguide.service.TripDealService;
import com.phildev.tourguide.service.UserService;
import com.phildev.tourguide.user.User;
import gpsUtil.GpsUtil;
import org.junit.jupiter.api.Test;
import rewardCentral.RewardCentral;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTripDealService {

    @Test
    public void getTripDeals() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        TripDealService tripDealService = new TripDealService();
        InternalTestHelper.setInternalUserNumber(0);
        UserService userService = new UserService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = tripDealService.getTripDeals(user);

        userService.tracker.stopTracking();

        assertEquals(5, providers.size());
    }

}
