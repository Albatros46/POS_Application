package com.sakcadag.service.Impl;

import com.sakcadag.dto.RegisterRequest;
import com.sakcadag.entity.*;

import com.sakcadag.exception.BusinessRuleException;
import com.sakcadag.exception.InvalidRequestException;
import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.RoleRepository;
import com.sakcadag.respository.UserRepository;
import com.sakcadag.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements IUserService {
    // BCrypt only uses the first 72 bytes of a password, so anything longer would be silently truncated.
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new InvalidRequestException("Password is too long");
        }
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new BusinessRuleException("Username already exists");
        }
        Role cashier = roleRepository.findByName("CASHIER")
                .orElseThrow(() -> new IllegalStateException("CASHIER role is missing"));

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(new HashSet<>(Set.of(cashier)));
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
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && user.getUsername().equals(authentication.getName())) {
            throw new BusinessRuleException("You cannot delete your own account");
        }
        userRepository.delete(user);
    }
}
