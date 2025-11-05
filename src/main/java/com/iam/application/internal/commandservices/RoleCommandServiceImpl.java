package com.iam.application.internal.commandservices;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.iam.domain.model.commands.SeedRolesCommand;
import com.iam.domain.model.entities.Role;
import com.iam.domain.model.valueobjects.Roles;
import com.iam.domain.services.RoleCommandService;
import com.iam.infrastructure.persistence.jpa.repositories.RoleRepository;

import java.util.Arrays;

@Service
@Transactional
public class RoleCommandServiceImpl implements RoleCommandService {
    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(Roles.values()).forEach(r -> {
            if (!roleRepository.existsByName(r)) roleRepository.save(new Role(r));
        });
    }
}
