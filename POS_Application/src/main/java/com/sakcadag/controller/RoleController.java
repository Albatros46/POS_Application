package com.sakcadag.controller;

import com.sakcadag.entity.Role;
import com.sakcadag.service.interfaces.IRoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    @Autowired
    private IRoleService roleService;

    @PostMapping("/add-new")
    public ResponseEntity<Role> createRole(@Valid @RequestBody Role role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.saveRole(role));
    }

    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{id}")
    public Role getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id);
    }

    @PutMapping("/{id}")
    public Role updateRole(@PathVariable Long id, @Valid @RequestBody Role role) {
        return roleService.updateRole(id, role);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
