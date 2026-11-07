package com.sakcadag.service.interfaces;

import com.sakcadag.entity.Role;

import java.util.List;

public interface IRoleService {
    Role saveRole(Role role);
    List<Role> getAllRoles();
    Role getRoleById(Long id);
    Role updateRole(Long id, Role role);
    void deleteRole(Long id);
}
