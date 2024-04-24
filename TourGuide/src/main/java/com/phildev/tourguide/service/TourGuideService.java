package com.phildev.tourguide.service;

import com.phildev.tourguide.dto.ClosestAttractionDTO;
import com.phildev.tourguide.helper.InternalTestHelper;
import com.phildev.tourguide.tracker.Tracker;
import com.phildev.tourguide.user.User;
import com.phildev.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;
import rewardCentral.RewardCentral;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {
	private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) {
        return (!user.getVisitedLocations().isEmpty()) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {

		return new ArrayList<>(internalUserMap.values());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for (Attraction attraction : gpsUtil.getAttractions()) {
			if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}


		return nearbyAttractions;
	}

	/**
	 * This method is sorting attractions by distance with a tree map and then adding 5 attractions in an array to have the 5 closest attraction from user current location
	 * @param visitedLocation which is th latest user location
	 * @return an array of {@link Attraction} with only 5 sorted by distance from user current location
	 */
	public List<Attraction> getFiveClosestAttractions(VisitedLocation visitedLocation){
		List<Attraction> attractions = gpsUtil.getAttractions();
		Map<Double,Attraction> attractionsWithDistanceFromUserLocation = new TreeMap<>();
		List<Attraction> closestAttractions = new ArrayList<>();
		// looping through attraction list and setting attraction and distance from user location sorted by distance as distance is the key of the tree map
		for(Attraction attraction : attractions){
			attractionsWithDistanceFromUserLocation.put(rewardsService.getDistance(attraction, visitedLocation.location), attraction);
		}
		int i = 0;
		// loop through tree map with attraction sorted by distance and get the first 5
		for(Attraction attraction : attractionsWithDistanceFromUserLocation.values()){
			// exit loop when 5 attractions are added to closestAttractions arrayList
			if(i == 5 ){
				break;
			}
			closestAttractions.add(attraction);
			i++;
		}
		return closestAttractions;
	}

	/**
	 * This method retrieves the five closest attractions from user location with infos such as user location and attraction name ,
	 * location, distance from user and reward points 	 *
	 * @param visitedLocation which is the latest location of the user
	 * @param user which is an Obkect of type {@link User} from which we retrieve the user id to calculate rewards for an attraction
	 * @return ClosestAttractionDto which contains an Object {@link gpsUtil.location.Location} which is the user location
	 * and a List<Map>String, Object>> representing the info about each attraction
	 */
	public ClosestAttractionDTO getFiveClosestAttractionsWithInfos(VisitedLocation visitedLocation, User user){
		// retrieve 5 closest attractions to user location
		List<Attraction> closestAttractions = getFiveClosestAttractions(visitedLocation);

		ClosestAttractionDTO closestAttractionsInfos = new ClosestAttractionDTO();
		closestAttractionsInfos.setUserLocation(visitedLocation.location);
		List<Map<String, Object>> attractionsInfo = closestAttractions.stream().map(attraction -> {
			//adding info for each attraction in a map
			Map<String, Object> attractionInfo = new HashMap<>();
			attractionInfo.put("name", attraction.attractionName);
			attractionInfo.put("longitude/latitude", attraction.longitude+"/"+attraction.latitude);
			attractionInfo.put("distance", rewardsService.getDistance(attraction, visitedLocation.location));
			attractionInfo.put("rewardPoints", new RewardCentral().getAttractionRewardPoints(attraction.attractionId, user.getUserId()));
			return attractionInfo;
		}).toList();
		// set the list of attractions info in the DTO Object
		closestAttractionsInfos.setClosestAttractions(attractionsInfo);

		return closestAttractionsInfos;

	}



	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
