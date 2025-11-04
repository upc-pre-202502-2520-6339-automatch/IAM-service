package com.iam.interfaces.rest.transform;


import com.iam.domain.model.commands.SignInCommand;
import com.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource signInResource) {
        return new SignInCommand(signInResource.username(), signInResource.password());
    }
}
