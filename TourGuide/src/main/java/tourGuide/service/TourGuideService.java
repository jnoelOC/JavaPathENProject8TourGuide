package tourGuide.service;

import java.util.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import tourGuide.dto.AttractionDTO;
import tourGuide.repository.UserRepository;
import tourGuide.tracker.Tracker;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);


	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;

	final Integer NbOfClosest = 5;

	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;




	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			// initializeInternalUsers
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}
	
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}
	
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;
	}




/*
	public void addUser(User user) {
		userService.addUser(user);
		/*if(!userRepository.internalUserMap.containsKey(user.getUserName())) {
			userRepository.internalUserMap.put(user.getUserName(), user);
		}*/
/*	}
	public List<User> getAllUsers() {
		//return userRepository.internalUserMap.values().stream().collect(Collectors.toList());
		return userService.getAllUsers();
	}
*/



	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(userRepository.tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}
	
	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		// verifier les sleep et sleeplighter dans gpsutil et dans calculateRewards()
		// paralleliser asynchrone le getuserloc et le calculateRewards
		//
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}


	//////////////////////////////////////  Get the closest five tourist attractions to the user //////////////////////

	private Integer[] retrievePositions(Map<Integer, Double> allSortedDistances){
		Integer idx = 0;
		Integer positions[] = new Integer[NbOfClosest];

		Set set = allSortedDistances.entrySet();
		Iterator i = set.iterator();

		while(i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			if(idx < NbOfClosest) {
				//ret += me.getKey() + ": " + me.getValue() + " ";
				positions[idx] = (Integer) me.getKey();
			}
			idx++;
		}

		return positions;
	}

	private Map setAllAttractionNames(Map<Integer, Double> allSortedDistances, Integer position, List<Attraction> allAttractions){
		Integer idx = 0;
		Integer positions[] = new Integer[NbOfClosest];

		Hashtable<Integer, String> allAttractionNames = new Hashtable<>();

		positions = retrievePositions(allSortedDistances);

		// recuperer la map des noms des attractions
		for(Integer index = 0; index < NbOfClosest; index++) {
			idx = 0;
			for(Attraction attraction : allAttractions) {

				if (position < NbOfClosest) {
					if (positions[position].equals(idx)) {
						allAttractionNames.put(positions[position], attraction.attractionName);
						position++;
					}
					idx++;
				}
			}
		}

		return allAttractionNames;
	}


	private Map findAllDistances(VisitedLocation visitedLocation, Integer position, List<Attraction> allAttractions){
		Hashtable<Integer, Double> allDistances = new Hashtable<>();

		for(Attraction attraction : allAttractions) {
			allDistances.put(position, rewardsService.getDistance(visitedLocation.location, attraction));
			position++;
		}

		return allDistances;
	}

	private static Map<Integer, Double> sortAllDistancesWithPositions( Map<Integer, Double> map ){
		List<Map.Entry<Integer, Double>> list =
				new LinkedList<Map.Entry<Integer, Double>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<Integer, Double>>(){
			public int compare( Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2 ){
				return (o1.getValue()).compareTo( o2.getValue());
			}
		});

		HashMap<Integer, Double> map_after = new LinkedHashMap<Integer, Double>();
		for(Map.Entry<Integer, Double> entry : list)
			map_after.put( entry.getKey(), entry.getValue() );
		return map_after;
	}

	//  Get the closest five tourist attractions to the user - no matter how far away they are.
	//  Return a new JSON object that contains:
	// Name of Tourist attraction,
	// Tourist attractions lat/long,
	// The user's location lat/long,
	// The distance in miles between the user's location and each of the attractions.
	// The reward points for visiting each Attraction.
	//    Note: Attraction reward points can be gathered from RewardsCentral
	public List<AttractionDTO> getClosest5Attractions(VisitedLocation visitedLocation, User user){

		List<AttractionDTO> attractionsDto = new ArrayList<>();
		Map<Integer, Double> allDistances = new Hashtable<>();
		Map<Integer, Double> allSortedDistances = new HashMap<>();
		Map<Integer, String> allAttractionNames;

		List<Attraction> allAttractions = gpsUtil.getAttractions();
		Integer position = 0;
		allDistances = findAllDistances(visitedLocation, position, allAttractions);
		allSortedDistances = sortAllDistancesWithPositions(allDistances);

		Integer positions[] = new Integer[NbOfClosest];
		Integer idx =0;
		position = 0;
		allAttractionNames  = setAllAttractionNames(allSortedDistances, position, allAttractions);
		positions = retrievePositions(allSortedDistances);

		for(Integer index = 0; index < NbOfClosest; index++) {
			idx = 0;
			for (Attraction attraction : allAttractions) {
				if (position < NbOfClosest) {
					if (positions[position].equals(idx)) {
						AttractionDTO attractionDTO = new AttractionDTO(user.getUserName(), visitedLocation,
								rewardsService.getDistance(visitedLocation.location, attraction),
								new UserReward(visitedLocation, attraction, rewardsService.getRewardPoints(attraction, user)));

						attractionsDto.add(attractionDTO);
						position++;
					}
					idx++;
				}
			}
		}

		return attractionsDto;
	}

	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for(Attraction attraction : gpsUtil.getAttractions()) {
			if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}
		return nearbyAttractions;
	}
	
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}
	

	
}
