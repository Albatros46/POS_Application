package com.sakcadag.service.Impl;

import com.sakcadag.entity.*;

import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.UserRepository;
import com.sakcadag.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    public User updateUser(Long id, User user) {
        User existing = getUserById(id);
        existing.setUsername(user.getUsername());
        existing.setPassword(user.getPassword());
        existing.setRoles(user.getRoles());
        return userRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.delete(getUserById(id));
    }
}
