package com.phildev.tourguide.service;


import com.phildev.tourguide.user.User;
import com.phildev.tourguide.user.UserReward;
import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;
import java.util.UUID;

@Service
public class TripDealService {

    // api-key used for test purpose
    private static final String tripPricerApiKey = "test-server-api-key";

    private final TripPricer tripPricer = new TripPricer();

    /**
     * This method is returning a list of Providers with special offers based on user preferences and reward points
     * It is calling {@link TripPricer#getPrice(String, UUID, int, int, int, int)} method
     * @param user
     * @return a list of {@link Provider}
     */
    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }
}
