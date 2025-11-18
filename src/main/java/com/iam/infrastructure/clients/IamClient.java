package com.iam.infrastructure.clients;

import com.iam.interfaces.rest.resources.UserResource;
import com.iam.interfaces.rest.resources.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "iam-service", path = "/api/v1/authentication/users")
public interface IamClient {

    @GetMapping("/{id}")
    UserResource getUser(@PathVariable Long id);
}
