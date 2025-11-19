package com.iam.application.internal.commandservices;



import com.iam.domain.exceptions.ResourceNotFoundException;
import com.iam.infrastructure.messaging.UserEventsProducer;
import com.iam.infrastructure.messaging.events.UserRegisteredEvent;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import com.iam.application.internal.outboundservices.hashing.HashingService;
import com.iam.application.internal.outboundservices.tokens.TokenService;
import com.iam.domain.model.aggregates.User;
import com.iam.domain.model.commands.RefreshTokenCommand;
import com.iam.domain.model.commands.SignInCommand;
import com.iam.domain.model.commands.SignUpCommand;
import com.iam.domain.model.entities.Role;
import com.iam.domain.services.UserCommandService;
import com.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;
    private final UserEventsProducer userEventsProducer;




    public UserCommandServiceImpl(UserRepository userRepository, HashingService hashingService, TokenService tokenService, RoleRepository roleRepository, UserEventsProducer userEventsProducer) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.userEventsProducer = userEventsProducer;
    }

    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username()))
            throw new IllegalStateException("Username already exists");

        var roles = resolveRoles(command.roles());
        var user = new User(command.username(), hashingService.encode(command.password()), roles);
        userRepository.save(user);

        var saved = userRepository.findByUsername(command.username());

        // 👉 Emitimos evento
        saved.ifPresent(u -> {
            var event = new UserRegisteredEvent(
                    u.getId(),
                    u.getUsername(),
                    u.getSerializedRoles()
            );
            userEventsProducer.publishUserRegistered(event);
        });

        return saved;
    }

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!hashingService.matches(command.password(), user.getPassword()))
            throw new BadCredentialsException("Invalid credentials");

        // 👉 Ahora metemos id + roles en el token
        var token = tokenService.generateToken(
                user.getUsername(),
                user.getId(),
                user.getSerializedRoles()      // List<String> ["SELLER", "BUYER"...]
        );
        return Optional.of(ImmutablePair.of(user, token));
    }


    @Override
    public Optional<ImmutablePair<User, String>> handle(RefreshTokenCommand command) {
        var username = tokenService.getUsernameFromToken(command.token());
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var token = tokenService.generateToken(
                user.getUsername(),
                user.getId(),
                user.getSerializedRoles()
        );
        return Optional.of(ImmutablePair.of(user, token));
    }


    private List<Role> resolveRoles(List<Role> requested) {
        // Si no envían roles, usa el rol por defecto
        if (requested == null || requested.isEmpty()) {
            var defaultRole = roleRepository.findByName(Role.getDefaultRole().getName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Default role " + Role.getDefaultRole().getName().name() + " not found"));
            return List.of(defaultRole);
        }

        var result = new ArrayList<Role>(requested.size());
        for (var r : requested) {
            var found = roleRepository.findByName(r.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Role " + r.getName().name() + " not found"));
            result.add(found);
        }
        return result;
    }





}
