package com.sakcadag.service.interfaces;

import com.sakcadag.entity.*;
import java.util.List;
import java.util.Optional;

public interface IUserService {
    User saveUser(User user);
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
}