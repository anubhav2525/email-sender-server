package com.scheduler.email.utils;

import com.scheduler.email.exceptions.custom.UnAuthorizedException;
import com.scheduler.email.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserInfoService {
    public UUID getUserLoggedInUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    public String getUserLoggedInUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUsername();
        }
        return null;
    }

    public UUID checkUserLoggedInByUserId() {
        UUID loggedInUserId = getUserLoggedInUserId();
        if (loggedInUserId == null) {
            throw new UnAuthorizedException();
        }
        return loggedInUserId;
    }

    public String checkUserLoggedInByUserEmail() {
        String loggedInUserEmail = getUserLoggedInUserEmail();
        if (loggedInUserEmail == null) {
            throw new UnAuthorizedException();
        }
        return loggedInUserEmail;
    }
}
