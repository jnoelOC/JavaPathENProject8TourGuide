package tourGuide.dto;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.model.UserReward;

import java.util.UUID;

public class AttractionDTO {
    private String userName;

    private VisitedLocation visitedLocation;

    private Double distanceInMiles;

    private UserReward rewardPoints;


    public AttractionDTO(String username, VisitedLocation visitedLocation, Double distanceInMiles, UserReward rewardPoints) {
        this.userName = username;
        this.visitedLocation = visitedLocation;
        this.distanceInMiles = distanceInMiles;
        this.rewardPoints = rewardPoints;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public VisitedLocation getVisitedLocation() {
        return visitedLocation;
    }

    public void setVisitedLocation(VisitedLocation visitedLocation) {
        this.visitedLocation = visitedLocation;
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
