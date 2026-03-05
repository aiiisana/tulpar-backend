package kz.diploma.tulpar.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Spring Security authentication token backed by a verified Firebase claim.
 */
public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final UserPrincipal principal;

    public FirebaseAuthenticationToken(UserPrincipal principal) {
        super(principal.getAuthorities());
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override public UserPrincipal getPrincipal() { return principal; }
    @Override public Object getCredentials()       { return null; }
}
