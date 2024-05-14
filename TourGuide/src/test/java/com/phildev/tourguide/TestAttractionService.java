package com.phildev.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.phildev.tourguide.helper.InternalTestHelper;
import com.phildev.tourguide.service.AttractionService;
import com.phildev.tourguide.service.RewardsService;
import com.phildev.tourguide.service.UserService;
import com.phildev.tourguide.user.User;
import org.junit.jupiter.api.Test;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;


public class TestAttractionService {



	@Test
	public void getNearbyAttractions() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		AttractionService attractionService = new AttractionService(gpsUtil, rewardsService);
		InternalTestHelper.setInternalUserNumber(0);
		UserService userService = new UserService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = userService.trackUserLocation(user).join();

		List<Attraction> attractions = attractionService.getFiveClosestAttractions(visitedLocation);

		userService.tracker.stopTracking();

		assertEquals(5, attractions.size());
	}



}
