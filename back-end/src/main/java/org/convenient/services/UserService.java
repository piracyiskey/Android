package org.convenient.services;

import org.convenient.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.convenient.models.User;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean isEmailTaken(String email) {
        return userRepository.countByEmail(email) > 0;
    }

    public String generateNextUserId() {
        Integer maxNumeric = userRepository.getMaxNumericId();
        int nextNumber = (maxNumeric != null) ? maxNumeric + 1 : 1;
        return String.format("us%02d", nextNumber);
    }

    public int registerUser(String email, String password, String fullName) {
        String id = generateNextUserId();
        String defaultProfilePic = "https://my-lambda-artifacts-s3-bucket.s3.amazonaws.com/images/profiles/default.png";
        return userRepository.registerNewUser(id, email, password, defaultProfilePic, fullName);
    }

    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null && BCrypt.checkpw(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return BCrypt.checkpw(rawPassword, user.getPassword());
    }

    public String getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return user.getId();
        }
        throw new RuntimeException("User not found for email: " + email);
    }


    public boolean updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashed);
            userRepository.save(user);
            return true;
        }
        return false;
    }

}
