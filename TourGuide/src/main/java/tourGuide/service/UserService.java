package tourGuide.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.model.User;
import tourGuide.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;



    public User getUser(String userName) {
        return userRepository.internalUserMap.get(userName);
    }




}
