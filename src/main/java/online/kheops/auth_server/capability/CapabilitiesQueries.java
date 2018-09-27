package online.kheops.auth_server.capability;

import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class CapabilitiesQueries {

    private CapabilitiesQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static Capability findCapabilityByCapabilityToken(String secret, EntityManager em)
            throws NoResultException {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where c.secret = :secret", Capability.class);
        query.setParameter("secret", secret);
        return query.getSingleResult();
    }

    public static Capability findCapabilityByCapabilityTokenandUser(User user, String secret, EntityManager em)
            throws NoResultException {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :user = c.user AND :secret = c.secret", Capability.class);
        query.setParameter("user", user);
        query.setParameter("secret", secret);
        return query.getSingleResult();
    }

    public static  List<Capability> findCapabilitiesByUserWithRevoke(User user, EntityManager em) {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :user = c.user", Capability.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public static  List<Capability> findCapabilitiesByUserWitoutRevoke(User user, EntityManager em) {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :user = c.user and c.revokedTime <> null", Capability.class);
        query.setParameter("user", user);
        return query.getResultList();
    }
}