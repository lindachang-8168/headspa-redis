package com.headspa.redis.service;

import com.headspa.redis.model.User;
import com.headspa.redis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User update(Long id, User updated) {
        return userRepository.findById(id).map(user -> {
            user.setName(updated.getName());
            user.setPhone(updated.getPhone());
            user.setEmail(updated.getEmail());
            return userRepository.save(user);
        }).orElse(null);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
