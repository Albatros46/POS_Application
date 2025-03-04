package com.sakcadag.service.Impl;

import com.sakcadag.entity.*;

import com.sakcadag.respository.UserRepository;
import com.sakcadag.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUser(Long id, User user) {
        return userRepository.findById(id)
                .map(existingUser->{
                    user.setId(id);
                    return userRepository.save(user);
                }).orElseThrow(()->new RuntimeException("User with id \" + id + \" not found\""));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}