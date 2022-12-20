package tourGuide.service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


import gpsUtil.location.Location;
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

	static final Integer NbOfClosest = 5;

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
		// ATTENTION : isEmpty fait en sorte que on perd la langue du pays c-a-d . ou , dans longitude de gpsUtil.jar
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;
	}






	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(UserRepository.tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
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

	//////////////////////////////////////  Get all user's recent locations and UUID couple //////////////////////
	public  Map<UUID, Location> FillCouple(List<User> users){
		Map<UUID, Location> couple = new HashMap<>();

		/*for(User oneUser : users){
			couple.put(oneUser.getUserId(), oneUser.getLastVisitedLocation().location);
		}*/

		// parallel plus lent avec 100 users
		users.parallelStream().forEach(user -> couple.put(user.getUserId(), user.getLastVisitedLocation().location));

		// parallel : 11 sec, 9 sec pour 1000 users. pour 5000 users : 12 sec. pour 10000 users : 6 sec
		// sequentiel : 08 sec, 7 sec pour 1000 users. pour 5000 users : 16 sec. pour 10000 users : 21 sec

		// Au-dela de 5000 users, la paralell devient plus rapide que le sequentiel

		return couple;
	}

	//////////////////////////////////////  Get the closest five tourist attractions to the user //////////////////////

	private List<Integer> retrievePositions(Map<Integer, Double> allSortedDistances){
		Integer idx = 0;
		List<Integer> listOfPositions = new ArrayList<>();

		Set<Map.Entry<Integer, Double>> set = allSortedDistances.entrySet();
		Iterator<Map.Entry<Integer, Double>> i = set.iterator();

		while(i.hasNext()) {
			Map.Entry<Integer, Double> me = i.next();
			if(idx < NbOfClosest) {
				listOfPositions.add(me.getKey());
			}
			idx++;
		}
		return listOfPositions;
	}

	private Map<Integer, Double> findAllDistances(VisitedLocation visitedLocation, Integer position, List<Attraction> allAttractions){
		HashMap<Integer, Double> allDistances = new HashMap<>();

		for(Attraction attraction : allAttractions) {
			allDistances.put(position, rewardsService.getDistance(visitedLocation.location, attraction));
			position++;
		}

		return allDistances;
	}

	private static Map<Integer, Double> sortAllDistancesWithPositions( Map<Integer, Double> map ){
		List<Map.Entry<Integer, Double>> list =	new LinkedList<>( map.entrySet() );

		Collections.sort( list, new Comparator<Map.Entry<Integer, Double>>(){
			public int compare( Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2 ){
				return (o1.getValue()).compareTo( o2.getValue());
			}
		});



		HashMap<Integer, Double> map_after = new LinkedHashMap<>();
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

		List<Attraction> allAttractions = gpsUtil.getAttractions();

		Integer position = 0;
		Map<Integer, Double> allDistances = findAllDistances(visitedLocation, position, allAttractions);

		Map<Integer, Double> allSortedDistances = sortAllDistancesWithPositions(allDistances);

		List<Integer> listOfPositions= retrievePositions(allSortedDistances);

		AtomicReference<Integer> numeroAttract = new AtomicReference<>(0);
		AtomicReference<Integer> finalNumeroAttract = numeroAttract;

		allAttractions.stream().forEach(attrac -> {
			if(listOfPositions.contains(finalNumeroAttract.get())) {

				AttractionDTO attractionDTO = new AttractionDTO(user.getUserName(), user.getLastVisitedLocation().location,
						attrac.longitude, attrac.latitude,
						rewardsService.getDistance(user.getLastVisitedLocation().location, attrac),
						new UserReward(visitedLocation, attrac, rewardsService.getRewardPoints(attrac, user)));

				attractionsDto.add(attractionDTO);
			}
			finalNumeroAttract.getAndSet(finalNumeroAttract.get() + 1);
		});

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
