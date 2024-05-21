package com.phildev.tourguide.service;

import com.phildev.tourguide.helper.InternalTestHelper;
import com.phildev.tourguide.tracker.Tracker;
import com.phildev.tourguide.user.User;
import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;

    public final Tracker tracker;
    boolean testMode = true;


    /**
     * This constructor is initializing a list of Users
     * It calls {@link Tracker#Tracker(UserService)} which is getting random visited locations for each of them
     * @param gpsUtil which an external app returning user locations
     * @param rewardsService which is an external app calculating user reward points
     */
    public UserService(GpsUtil gpsUtil, RewardsService rewardsService) {
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


    /**
     * This method is calling {@link #trackUserLocation(User)} if user has no visited location
     * and return a {@link VisitedLocation}
     * @param user
     * @return a {@link VisitedLocation}
     */
    public VisitedLocation getUserLocation(User user) {
        return (!user.getVisitedLocations().isEmpty()) ? user.getLastVisitedLocation()
                : trackUserLocation(user).join();
    }

    /**
     * This method is returning a user by passing its username
     * @param userName
     * @return a {@link User}
     */
    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    /**
     * This method is getting a list of users from {@link #internalUserMap} by calling the values() method
     *  @return a list of {@link User}
     */
    public List<User> getAllUsers() {

        return new ArrayList<>(internalUserMap.values());
    }

    /**
     * This method is adding a user to the internal map {@link #internalUserMap} if the user does not already exist
     * @param user
     */
    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    /**
     * This method is chaining different tasks by first getting user current location
     * then adding this location to user visited location and then calculating the reward points to be added if necessary
     * @param user
     * @return a CompletableFuture<VisitedLocation> which is a task running in the background that is run within a forkjoin pool improving performance of the app
     */
    public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
        CompletableFuture<VisitedLocation> visitedLocation = CompletableFuture.supplyAsync(()->gpsUtil.getUserLocation(user.getUserId()));
        visitedLocation.thenAccept(user::addToVisitedLocations)
                .thenRun(()->rewardsService.calculateRewards(user));
        return visitedLocation;
    }

    /**
     * This method is used to create a set of users and create random visited locations for test purpose
     * It is adding users to an internal map {@link #internalUserMap}
     * The number of users added is based on {@link InternalTestHelper#internalUserNumber}
     */
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

    /**
     * This method is used in testing mode to generate three random visited location for a user
     * @param user
     */
    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
                    new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    /**
     * This method is returning a random longitude which is used in method {@link #generateUserLocationHistory(User)}
     * @return a double as a longitude
     */
    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    /**
     * This method is returning a random latitude which is used in method {@link #generateUserLocationHistory(User)}
     * @return a double as a latitude
     */
    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    /**
     * This method is returning a random Date used in method {@link #generateUserLocationHistory(User)}
     * @return a Date
     */
    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    /**
     * This method is shutting down the executor service launched in {@link Tracker#Tracker(UserService)}
     */
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

    // Database connection will be used for external users, but for testing purposes
    // internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();


}
