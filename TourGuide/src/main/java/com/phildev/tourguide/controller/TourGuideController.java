package com.phildev.tourguide.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TourGuideController {

    @GetMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
}
