package com.phildev.tourguide.dto;

import gpsUtil.location.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


@Getter
@Setter
public class ClosestAttractionDTO {

    private Location userLocation;
    List<Map<String , Object>> closestAttractions;
}
