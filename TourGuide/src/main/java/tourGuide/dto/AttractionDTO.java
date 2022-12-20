package tourGuide.dto;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.model.UserReward;

import java.util.UUID;

public class AttractionDTO {
    private String userName;

    private Location userLocation;


    private Double attractionLongitude;

    private Double attractionLatitude;

    private Double distanceInMiles;

    private UserReward rewardPoints;


    public AttractionDTO(String username, Location userLocation, Double attractionLongitude, Double attractionLatitude, Double distanceInMiles, UserReward rewardPoints) {
        this.userName = username;
        this.userLocation = userLocation;
        this.attractionLongitude = attractionLongitude;
        this.attractionLatitude = attractionLatitude;
        this.distanceInMiles = distanceInMiles;
        this.rewardPoints = rewardPoints;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public Double getAttractionLongitude() {
        return attractionLongitude;
    }

    public void setAttractionLongitude(Double attractionLongitude) {
        this.attractionLongitude = attractionLongitude;
    }

    public Double getAttractionLatitude() {
        return attractionLatitude;
    }

    public void setAttractionLatitude(Double attractionLatitude) {
        this.attractionLatitude = attractionLatitude;
    }

    public Double getDistanceInMiles() {
        return distanceInMiles;
    }

    public void setDistanceInMiles(Double distanceInMiles) {
        this.distanceInMiles = distanceInMiles;
    }

    public UserReward getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(UserReward rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
}
