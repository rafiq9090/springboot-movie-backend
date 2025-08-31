package org.example.movices.repository;

import org.example.movices.model.entity.Role;
import org.example.movices.model.entity.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // ... existing code ...
    Optional<Role> findByName(RoleType name);
}
