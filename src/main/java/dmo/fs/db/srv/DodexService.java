
package dmo.fs.db.srv;

import dmo.fs.entities.Message;
import dmo.fs.entities.Undelivered;
import dmo.fs.entities.User;
import dmo.fs.entities.User_;
import dmo.fs.util.MessageUser;
import jakarta.json.bind.JsonbBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import jakarta.websocket.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DodexService {
    private final static Logger logger = LogManager.getLogger(DodexService.class.getName());

    private static EntityManager entityManager;

    public static void setupSql(EntityManager entityManager) {
        DodexService.entityManager = entityManager;
    }

    private static List<Map<String, String>> getAllUsers(MessageUser messageUser, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> userQuery = builder.createQuery(User.class);
        Root<User> root = userQuery.from(User.class);

        ParameterExpression<String> value = builder.parameter(String.class, User_.NAME);
        userQuery.select(root).where(builder.notEqual(root.<String>get(User_.NAME), value));

        TypedQuery<User> query = entityManager.createQuery(userQuery);
        query.setParameter(User_.NAME, messageUser.getName());
        List<User> results = query.getResultList();
        List<Map<String, String>> userList = new ArrayList<>();

        results.forEach(user -> {
            Map<String, String> userMap = new HashMap<>();
            userMap.put(User_.NAME, user.getName());
            userList.add(userMap);
        });
        return userList;
    }

    public static User getUserByName(String name, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> userQuery = builder.createQuery(User.class);
        Root<User> root = userQuery.from(User.class);

        userQuery.select(root).where(builder.equal(root.get(User_.NAME), name));
        TypedQuery<User> query = entityManager.createQuery(userQuery);

        return query.getSingleResult();
    }

    public User getUserById(MessageUser messageUser, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> userQuery = builder.createQuery(User.class);
        Root<User> root = userQuery.from(User.class);

        Predicate equalName = builder.equal(root.get(User_.NAME), messageUser.getName());
        Predicate equalPassword = builder.equal(root.get(User_.PASSWORD), messageUser.getPassword());
        userQuery.select(root).where(builder.and(equalName, equalPassword));

        TypedQuery<User> query = entityManager.createQuery(userQuery);

        return query.getSingleResult();
    }

    public List<Message> getUserUndelivered(MessageUser messageUser, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Message> criteriaQuery = builder.createQuery(Message.class);

        Root<User> userRoot = criteriaQuery.from(User.class);
        Join<User, Message> users = userRoot.join(User_.MESSAGES);
        criteriaQuery.where(builder.equal(userRoot.get(User_.ID), messageUser.getId()));

        return entityManager
            .createQuery(criteriaQuery.select(users))
            .getResultList();
    }

    public Long addMessage(Session ws, MessageUser messageUser, String mess) {
        EntityManager em = entityManager.getEntityManagerFactory().createEntityManager(); // get a connection

        Message message = new Message();

        message.setPostDate(LocalDateTime.now());
        message.setMessage(mess);
        message.setFromHandle(messageUser.getName());

        em.getTransaction().begin();
        em.persist(message);
        messageUser.setEntityManager(em);

        return message.getId();
    }

    private void persistUndelivered(Long userId, Long messageId, EntityManager entityManager) {
        Undelivered undelivered = new Undelivered();
        Undelivered.UndeliveredId undeliveredId = new Undelivered.UndeliveredId(userId, messageId);
        undelivered.setUndeliveredId(undeliveredId);

        entityManager.persist(undelivered);
    }

    public void addUndelivered(Session ws, MessageUser messageUser, List<String> destination, Long messageId) {
        EntityManager em = messageUser.getEntityManager();

        for (String name : destination) {
            User user = getUserByName(name, em);
            persistUndelivered(user.getId(), messageId, em);
        }
        em.getTransaction().commit();
        em.clear(); // detach all entities
        em.close();
    }

    @Transactional
    public MessageUser selectUser(MessageUser messageUser, Session ws)
        throws IOException {
        EntityManager em = entityManager.getEntityManagerFactory().createEntityManager(); // get a connection

        messageUser.setEntityManager(em);

        try {
            User user = getUserById(messageUser, em);
            user.setLastLogin(LocalDateTime.now());

            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();

            messageUser.setLastLogin(user.getLastLogin());
            messageUser.setId(user.getId());
        } catch (NoResultException nre) { // add user on not found exception
            messageUser.setLastLogin(LocalDateTime.now());
            if (messageUser.getIp() == null) {
                messageUser.setIp("Unknown");
            }

            User user = new User();
            user.setLastLogin(messageUser.getLastLogin().toLocalDateTime());
            user.setName(messageUser.getName());
            user.setPassword(messageUser.getPassword());
            user.setIp(messageUser.getIp());
            if (messageUser.getIp() == null) {
                user.setIp("Unknown");
            }
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            messageUser.setId(user.getId());
            messageUser.setLastLogin(user.getLastLogin());
        }
        return messageUser;
    }

    //
    public String buildUsersJson(MessageUser messageUser) throws Exception {
        EntityManager em = messageUser.getEntityManager();

        List<Map<String, String>> userList = getAllUsers(messageUser, em);

        try (jakarta.json.bind.Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.toJson(userList);
        }
    }

    private void removeMessage(Message message, EntityManager em) {
        Message currentMessage = em.find(Message.class, message.getId());

        if (currentMessage.getUndelivered().isEmpty()) {
            em.remove(currentMessage);
        }
    }

    private void removeUndelivered(Long userId, Long messageId, EntityManager entityManager) {
        Undelivered undelivered = entityManager.find(Undelivered.class, new Undelivered.UndeliveredId(userId, messageId));
        entityManager.remove(undelivered);
    }

    public Map<String, Integer> processUserMessages(Session ws, MessageUser messageUser) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd@HH");
        Map<String, Integer> counts = new ConcurrentHashMap<>();

        EntityManager em = messageUser.getEntityManager();

        List<Message> messageList = getUserUndelivered(messageUser, em);
        em.getTransaction().begin();
        for (Message message : messageList) {
            ws.getAsyncRemote().sendText(
                message.getFromHandle() + message.getPostDate().format(formatter) + " " + message.getMessage());
            removeUndelivered(messageUser.getId(), message.getId(), em);
        }

        em.flush();
        em.clear();

        for (Message message : messageList) {
            removeMessage(message, em);
        }
        em.getTransaction().commit();
        em.close();
        counts.put("messages", messageList.size());

        return counts;
    }

    public Long deleteUser(jakarta.websocket.Session ws, MessageUser messageUser) throws SQLException, InterruptedException {
        EntityManager em = entityManager.getEntityManagerFactory().createEntityManager();
        User user = getUserById(messageUser, em);
        em.getTransaction().begin();
        em.remove(user);
        em.getTransaction().commit();
        em.close();
        return user.getId();
    }
}
