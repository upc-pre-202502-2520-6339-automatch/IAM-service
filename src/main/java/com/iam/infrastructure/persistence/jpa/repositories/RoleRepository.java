package com.iam.infrastructure.persistence.jpa.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.iam.domain.model.entities.Role;
import com.iam.domain.model.valueobjects.Roles;

import java.util.Optional;

@Repository
public interface RoleRepository  extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Roles name);
    boolean existsByName(Roles name);
}
