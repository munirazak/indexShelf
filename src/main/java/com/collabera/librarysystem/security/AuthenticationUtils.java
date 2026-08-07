package com.collabera.librarysystem.security;

import com.collabera.librarysystem.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthenticationUtils {

    private AuthenticationUtils() {
    }

    public static String requireLibraryId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Authentication required");
        }
        return authentication.getName();
    }
}
