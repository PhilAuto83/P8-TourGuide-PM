package com.phildev.tourguide.service;

import com.phildev.tourguide.dto.ClosestAttractionDTO;
import com.phildev.tourguide.user.User;
import com.phildev.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.*;

@Service
public class AttractionService {
	private final Logger logger = LoggerFactory.getLogger(AttractionService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;

	public AttractionService(GpsUtil gpsUtil, RewardsService rewardsService){
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
	}

	/**
	 * This method is sorting attractions by distance with a tree map and then adding 5 attractions in an array to have the 5 closest attraction from user current location
	 * @param visitedLocation which is the latest user location
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

}
