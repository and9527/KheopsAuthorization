package online.kheops.auth_server.filter;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.AssertionVerifier;
import online.kheops.auth_server.assertion.BadAssertionException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.Users;
import online.kheops.auth_server.user.UsersPermission;

import javax.annotation.Priority;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContext;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecuredFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(SecuredFilter.class.getName());

    @Context
    ServletContext context;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        LOG.log(Level.SEVERE, "in SecuredFilter");

        LOG.log(Level.SEVERE, "URI:" + requestContext.getUriInfo().getPath());

        LOG.log(Level.SEVERE, "Headers:");
        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        for (String key: headers.keySet()) {
            headers.get(key).forEach(value -> LOG.log(Level.SEVERE, key + ": " + value));
        }

        final String token;
        try {
            token = getToken(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION));
        } catch (IllegalArgumentException e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        LOG.log(Level.SEVERE, "token: " + token);

        final Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(token);
        } catch (BadAssertionException e) {
            LOG.log(Level.WARNING, "Received bad assertion", e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        LOG.log(Level.SEVERE, "assertion: " + assertion);

        User user;
        try {
            user = Users.getUser(assertion.getUsername());
        } catch (UserNotFoundException e) {
            try {
                user = Users.getUser(assertion.getEmail());
            } catch (UserNotFoundException ex) {
                user = null;
            }
        }

        LOG.log(Level.SEVERE, "1 user: " + user);

        // if the user can't be found, try to build a new one;
        if (user == null) {
            // try to build a new user, building a new user might fail if there is a unique constraint violation
            // due to a race condition. Catch the violation and do nothing in that case.
            EntityManager em = EntityManagerListener.createEntityManager();
            EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();
                LOG.info("User not found, creating a new User");
                final User newUser = new User(assertion.getUsername(), assertion.getEmail());
                final Album inbox = new Album();
                inbox.setName("inbox");
                newUser.setInbox(inbox);
                final UsersPermission usersPermission = new UsersPermission();
                usersPermission.setInboxPermission();
                inbox.setPermission(usersPermission);

                final AlbumUser albumUser = new AlbumUser(inbox, newUser, false);
                albumUser.setNewCommentNotifications(false);
                albumUser.setNewSeriesNotifications(false);

                em.persist(inbox);
                em.persist(newUser);
                em.persist(albumUser);
                tx.commit();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Caught exception while creating a new user", e);
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }

            // At this point there should definitely be a user in the database for the calling user.
            try {
                user = Users.getUser(assertion.getUsername());
            } catch (UserNotFoundException e) {
                throw new IllegalStateException("Unable to find user");//TODO
            }

        }

        LOG.log(Level.SEVERE, "2 user: " + user);

        final long userPK = user.getPk();
        final boolean capabilityAccess = assertion.hasCapabilityAccess();
        final boolean isSecured = requestContext.getSecurityContext().isSecure();
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public KheopsPrincipal getUserPrincipal() {
                return new KheopsPrincipal(userPK);
            }

            @Override
            public boolean isUserInRole(String role) {
                if (role.equals("capability")) {
                    return capabilityAccess;
                }
                return false;
            }

            @Override
            public boolean isSecure() {
                return isSecured;
            }

            @Override
            public String getAuthenticationScheme() {
                return "BEARER";
            }
        });

        LOG.log(Level.SEVERE, "Finished secure filter");
    }

    private static String getToken(String authorizationHeader) {
        final String token;
        if (authorizationHeader != null) {

            if (authorizationHeader.toUpperCase().startsWith("BASIC ")) {
                final String encodedAuthorization = authorizationHeader.substring(6);

                final String decoded = new String(Base64.getDecoder().decode(encodedAuthorization), StandardCharsets.UTF_8);
                String[] split = decoded.split(":");
                if (split.length != 2) {
                    LOG.log(Level.WARNING, "Basic authentication doesn't have a username and password");
                    throw new IllegalArgumentException();
                }

                token = split[1];
            } else if (authorizationHeader.toUpperCase().startsWith("BEARER ")) {
                token = authorizationHeader.substring(7);
            } else {
                LOG.log(Level.WARNING, "Unknown authorization header");
                throw new IllegalArgumentException();
            }

            if (token.length() == 0) {
                LOG.log(Level.WARNING, "Empty authorization token");
                throw new IllegalArgumentException();
            }
        } else {
            LOG.log(Level.WARNING, "Missing authorization header");
            throw new IllegalArgumentException();
        }

        return token;
    }
}
