package com.sakcadag.service.Impl;

import com.sakcadag.entity.Role;
import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.RoleRepository;
import com.sakcadag.service.interfaces.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }

    @Override
    public Role updateRole(Long id, Role role) {
        Role existing = getRoleById(id);
        existing.setName(role.getName());
        return roleRepository.save(existing);
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.delete(getRoleById(id));
    }
}
