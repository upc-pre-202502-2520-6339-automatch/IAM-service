package com.iam.domain.services;



import org.apache.commons.lang3.tuple.ImmutablePair;
import com.iam.domain.model.aggregates.User;
import com.iam.domain.model.commands.RefreshTokenCommand;
import com.iam.domain.model.commands.SignInCommand;
import com.iam.domain.model.commands.SignUpCommand;

import java.util.Optional;

public interface UserCommandService {
    Optional<User> handle(SignUpCommand command);
    Optional<ImmutablePair<User, String>> handle(SignInCommand command);
    Optional<ImmutablePair<User, String>> handle(RefreshTokenCommand command);
}
