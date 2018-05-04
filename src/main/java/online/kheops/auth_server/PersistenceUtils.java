package online.kheops.auth_server;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class PersistenceUtils {

    private static String user;
    private static String password;
    private static String url;

    static void setUser(String user) {
        PersistenceUtils.user = user;
    }

    static void setPassword(String password) {
        PersistenceUtils.password = password;
    }

    static void setUrl(String url) {
        PersistenceUtils.url = url;
    }

    public static EntityManagerFactory createEntityManagerFactory() {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.user", user);
        properties.put("javax.persistence.jdbc.password", password);
        properties.put("javax.persistence.jdbc.url", url + "?useUnicode=yes&amp;characterEncoding=UTF-8");

        return Persistence.createEntityManagerFactory("online.kheops", properties);
    }
}
