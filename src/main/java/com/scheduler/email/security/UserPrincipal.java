package com.scheduler.email.security;

import com.scheduler.email.data.entities.auth.AppUser;
import com.scheduler.email.data.entities.auth.Permissions;
import com.scheduler.email.data.entities.auth.Roles;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserPrincipal implements UserDetails, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Getter
    private final UUID id;
    private final String email;
    private final String password;
    //    private final boolean enabled;
//    private final boolean deleted;
//    private final OffsetDateTime lockedUntil;
    private final Set<GrantedAuthority> authorities;


    private UserPrincipal(AppUser user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
//        this.lockedUntil = user.getLockedUntil();
        this.authorities = user.getRoles().stream()
                .filter(role -> Boolean.TRUE.equals(role.getEnabled()))
                .filter(role -> !Boolean.TRUE.equals(role.getDeleted()))
                .flatMap(this::roleAuthorities)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static UserPrincipal from(AppUser user) {
        return new UserPrincipal(user);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // TODO: Store the information in redis when user tries to failed atempts then lock
        return false;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    private Stream<GrantedAuthority> roleAuthorities(Roles role) {
        Stream<GrantedAuthority> roleName = Stream.of(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        Stream<GrantedAuthority> permissionNames = role.getPermissions().stream()
                .filter(permission -> Boolean.TRUE.equals(permission.getEnabled()))
                .filter(permission -> !Boolean.TRUE.equals(permission.getDeleted()))
                .map(Permissions::getName)
                .map(SimpleGrantedAuthority::new);
        return Stream.concat(roleName, permissionNames);
    }

}
