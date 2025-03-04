package com.sakcadag.service.Impl;

import com.sakcadag.entity.Role;
import com.sakcadag.respository.RoleRepository;
import com.sakcadag.service.interfaces.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class RoleServiceImpl implements IRoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public Role updateRole(Long id, Role role) {
        return roleRepository.findById(id)
                .map(existingRole -> {
                    role.setId(id);
                    return roleRepository.save(role);
                })
                .orElseThrow(() -> new RuntimeException("Role with id " + id + " not found"));
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
}
