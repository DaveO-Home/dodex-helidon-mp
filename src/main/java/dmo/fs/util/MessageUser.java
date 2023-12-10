package dmo.fs.util;

import jakarta.persistence.EntityManager;

import java.sql.Timestamp;

public interface MessageUser {
    void setId(Long id);
    void setName(String name);
    void setPassword(String password);
    void setIp(String ip);
    <T>void setLastLogin(T lastLogin);
    Long getId();
    String getName();
    String getPassword();
    String getIp();
    Timestamp getLastLogin();
    void setEntityManager(EntityManager em);
    EntityManager getEntityManager();
}