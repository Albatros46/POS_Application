package com.sakcadag.service.interfaces;

import com.sakcadag.entity.*;
import java.util.List;

public interface IUserService {
    User saveUser(User user);
    List<User> getAllUsers();
    User getUserById(Long id);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
}
