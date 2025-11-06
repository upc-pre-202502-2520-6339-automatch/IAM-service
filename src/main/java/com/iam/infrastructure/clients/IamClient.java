package com.iam.infrastructure.clients;

import com.iam.interfaces.rest.resources.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "iam", path = "/api/v1/users")
public interface IamClient {
    @GetMapping("/{id}")
    UserSummary getUser(@PathVariable Long id);
}