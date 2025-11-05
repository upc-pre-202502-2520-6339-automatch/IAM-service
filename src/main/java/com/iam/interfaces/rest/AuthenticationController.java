package com.iam.interfaces.rest;



import com.iam.application.internal.commandservices.TokenRevocationCommandService;
import com.iam.domain.model.commands.RevokeTokenCommand;
import com.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.iam.domain.model.commands.RefreshTokenCommand;
import com.iam.domain.services.UserCommandService;
import com.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.iam.interfaces.rest.resources.SignInResource;
import com.iam.interfaces.rest.resources.SignUpResource;
import com.iam.interfaces.rest.resources.UserResource;
import com.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

/**
 * AuthenticationController
 * <p>
 * This controller is responsible for handling authentication requests.
 * It exposes two endpoints:
 *     <ul>
 *         <li>POST /api/v1/authentication/sign-in</li>
 *         <li>POST /api/v1/authentication/sign-up</li>
 *     </ul>
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;
    private final BearerTokenService bearerTokenService;
    private final TokenRevocationCommandService tokenRevocationCommandService;



    public AuthenticationController(UserCommandService userCommandService, BearerTokenService bearerTokenService, TokenRevocationCommandService tokenRevocationCommandService) {
        this.userCommandService = userCommandService;
        this.bearerTokenService = bearerTokenService;
        this.tokenRevocationCommandService = tokenRevocationCommandService;
    }

    /**
     * Handles the sign-in request.
     *
     * @param body the sign-in request body.
     * @return the authenticated user resource.
     */
    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody @Valid SignInResource body) {
        var cmd = SignInCommandFromResourceAssembler.toCommandFromResource(body);
        var result = userCommandService.handle(cmd);
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var res = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(result.get().getLeft(), result.get().getRight());
        return ResponseEntity.ok(res);
    }


    /**
     * Handles the sign-up request.
     *
     * @param signUpResource the sign-up request body.
     * @return the created user resource.
     */
    @PostMapping("/sign-up")
    public ResponseEntity<UserResource> signUp(@RequestBody @Valid SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);

    }

    @PostMapping("/verify-token")
    public ResponseEntity<AuthenticatedUserResource> verifyTokenHeader(HttpServletRequest request) {
        String token = bearerTokenService.getBearerTokenFrom(request);
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        var refreshed = userCommandService.handle(new RefreshTokenCommand(token));
        if (refreshed.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        var res = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(refreshed.get().getLeft(), refreshed.get().getRight());
        return ResponseEntity.ok(res);
    }

    /** Refresh: usa el token actual si no está revocado y no expiró */
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticatedUserResource> refresh(HttpServletRequest request) {
        String token = bearerTokenService.getBearerTokenFrom(request);
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var refreshed = userCommandService.handle(new RefreshTokenCommand(token));
        if (refreshed.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var res = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(refreshed.get().getLeft(), refreshed.get().getRight());
        return ResponseEntity.ok(res);
    }

    /** Logout real: revoca el token vigente */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = bearerTokenService.getBearerTokenFrom(request);
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        tokenRevocationCommandService.handle(new RevokeTokenCommand(token));
        return ResponseEntity.noContent().build();
    }


}
