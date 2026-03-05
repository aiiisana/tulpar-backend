package kz.diploma.tulpar.security;

import kz.diploma.tulpar.domain.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Immutable principal populated from the verified Firebase ID token.
 * Stored in the Spring Security context for the lifetime of the request.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final String uid;       // Firebase UID
    private final String email;
    private final UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String uid, String email, UserRole role) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getUsername()  { return uid; }
    @Override public String getPassword()  { return null; } // no password stored
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return true; }
}
