package tourGuide.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.dto.AttractionDTO;
import tourGuide.service.TourGuideService;
import tourGuide.model.User;
import tourGuide.service.UserService;
import tripPricer.Provider;

@RestController
public class TourGuideController {
    private Logger logger = LoggerFactory.getLogger(TourGuideController.class);
	@Autowired
	TourGuideService tourGuideService;

    @Autowired
    UserService userService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(userService.getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
    }
    
    //  TODO: Change this method to no longer return a List of Attractions.
 	//  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
 	//  Return a new JSON object that contains:
    	// Name of Tourist attraction, 
        // Tourist attractions lat/long, 
        // The user's location lat/long, 
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
    @GetMapping("/getClosestFiveAttractions")
    public ResponseEntity<List<AttractionDTO>> getClosestFiveAttractions(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(userService.getUser(userName));
       // return JsonStream.serialize(tourGuideService.getClosest5Attractions(visitedLocation, userService.getUser(userName)));
        List<AttractionDTO> attractionsDto = null;

        attractionsDto = tourGuideService.getClosest5Attractions(visitedLocation, userService.getUser(userName));

        if (attractionsDto == null) {
            logger.error("Erreur dans getClosestFiveAttractions : status Non trouvé.");
            return new ResponseEntity<>(attractionsDto, HttpStatus.NOT_FOUND);
        } else {
            logger.info("getClosestFiveAttractions : Liste de attractions trouvées.");
            return new ResponseEntity<>(attractionsDto, HttpStatus.FOUND);
        }
    }

    @RequestMapping("/getNearbyAttractions") 
    public String getNearbyAttractions(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(userService.getUser(userName));
        return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
    }
    
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
    	return JsonStream.serialize(tourGuideService.getUserRewards(userService.getUser(userName)));
    }

    @GetMapping("/toto")
    public List<User> getAllUsers() {
        logger.info("start AllUsers");
        return userService.getAllUsers();
    }
    
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
    	// TODO: Get a list of every user's most recent location as JSON
    	//- Note: does not use gpsUtil to query for their current location, 
    	//        but rather gathers the user's current location from their stored location history.
    	//
    	// Return object should be the just a JSON mapping of userId to Locations similar to: Map ou List ?
    	//     {
    	//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371}
        //        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371}
        //        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371}
        //        ...
    	//     }
    	
    	return JsonStream.serialize("");
    }
    
    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
    	List<Provider> providers = tourGuideService.getTripDeals(userService.getUser(userName));
    	return JsonStream.serialize(providers);
    }
    
/*    private User getUser(String userName) {
    	return userService.getUser(userName);
    }
 */

}