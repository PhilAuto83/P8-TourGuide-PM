package com.phildev.tourguide.service;

import com.phildev.tourguide.user.User;
import com.phildev.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.Setter;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	@Setter
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;

	private static final ExecutorService executorService = Executors.newFixedThreadPool(50);
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/***
	 * This method is adding reward points if user has a visited location near an attraction and if the combination of attraction, visited location has no reward already attached to it
	 * @param user
	 * @return a Future which is a task adding reward points if user is close to an attraction
	 * The task is run in the background owing to an executor service to improve performance
	 */
	public Future<?> calculateRewards(User user) {
		return executorService.submit(()->{
			List<VisitedLocation> userLocations =user.getVisitedLocations();
			List<Attraction> attractions = gpsUtil.getAttractions();

			for (VisitedLocation visitedLocation : userLocations){
				for(Attraction attraction : attractions) {
					if(user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
						if(nearAttraction(visitedLocation, attraction)) {
							user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						}
					}
				}
			}
		});
	}

	/**
	 * This method is using {@link #getDistance(Location, Location)} method to calculate is a location is within attraction range with the variable {@link #attractionProximityRange}
	 * @param attraction
	 * @param location
	 * @return a boolean
	 */
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return !(getDistance(attraction, location) > attractionProximityRange);
	}

	/**
	 * This method is using {@link #getDistance(Location, Location)} method to calculate is a location is near attraction range with the variable {@link #proximityBuffer}
	 * @param visitedLocation
	 * @param attraction
	 * @return a boolean
	 */
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
	}

	/**
	 * This method is calling rewardCentral which is an external app to get reward points for a user depending on the Attraction passed as an argument
	 * @param attraction
	 * @param user
	 * @return an integer which is reward points
	 */
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	/**
	 * This method is calculating the distance between two locations passed as arguments
	 * @param loc1
	 * @param loc2
	 * @return a double which is the distance
	 */
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}

}
