package com.sakcadag.service.interfaces;

import com.sakcadag.dto.RegisterRequest;
import com.sakcadag.entity.*;
import java.util.List;

public interface IUserService {
    User register(RegisterRequest request);
    List<User> getAllUsers();
    User getUserById(Long id);
    void deleteUser(Long id);
}
