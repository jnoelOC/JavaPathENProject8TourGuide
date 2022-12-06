package tourGuide.service;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.model.User;
import tourGuide.repository.UserRepository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserRepository userRepository;


    public User getUser(String userName) {
        return userRepository.internalUserMap.get(userName);
    }


    public void addUser(User user) {
        if(!userRepository.internalUserMap.containsKey(user.getUserName())) {
            userRepository.internalUserMap.put(user.getUserName(), user);
        }
    }

    public List<User> getAllUsers() {

        return userRepository.internalUserMap.values().stream().collect(Collectors.toList());
    }

}
