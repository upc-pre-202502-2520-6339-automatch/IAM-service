package com.iam.infrastructure.hashing.bcrypt;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.iam.application.internal.outboundservices.hashing.HashingService;

public interface BCryptHashingService  extends HashingService, PasswordEncoder {

}
