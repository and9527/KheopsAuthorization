package online.kheops.auth_server.assertion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;

final class JWTAssertionBuilder implements AssertionBuilder {
    private static final String KHEOPS_ISSUER = "auth.kheops.online";
    private static final String SUPERUSER_ISSUER = "authorization.kheops.online";
    private static final String GOOGLE_ISSUER = "accounts.google.com";

    private final String superuserSecret;
    private final String authorizationSecret;

    JWTAssertionBuilder(String superuserSecret, String authorizationSecret) {
        this.superuserSecret = superuserSecret;
        this.authorizationSecret = authorizationSecret;
    }

    @Override
    public Assertion build(String assertionToken) throws BadAssertionException {
        final String issuer;

        try {
            issuer = JWT.decode(assertionToken).getIssuer();
        } catch (JWTDecodeException e) {
            throw new BadAssertionException("Unable to decode JWT", e);
        }

        if (issuer == null) {
            throw new BadAssertionException("JWT has no issuer");
        }

        switch (issuer) {
            case KHEOPS_ISSUER:
                return AuthorizationJWTAssertion.getBuilder(authorizationSecret).build(assertionToken);
            case SUPERUSER_ISSUER:
                return SuperuserJWTAssertion.getBuilder(superuserSecret).build(assertionToken);
            case GOOGLE_ISSUER:
                return GoogleJWTAssertion.getBuilder().build(assertionToken);
            default:
                throw new BadAssertionException("Unknown JWT issuer");
        }
    }
}