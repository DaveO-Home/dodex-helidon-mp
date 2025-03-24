package dmo.fs.db.srv;

import dmo.fs.db.DodexDatabaseBase;
import dmo.fs.db.fac.DbConfiguration;
import dmo.fs.entities.*;
import dmo.fs.util.DodexUtil;
import dmo.fs.util.MessageUser;
import io.helidon.security.SecurityContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequestScoped
public class GroupService implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    protected final static Logger logger = LogManager.getLogger(GroupService.class.getName());
    private final static DodexDatabaseBase dodexDatabase;

    @Inject
    @Context
    SecurityContext securityContext;

    static {
        try {
            dodexDatabase = DbConfiguration.getDefaultDb();
        } catch (InterruptedException | IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static Boolean isCheckForOwner;

    @Inject
    public GroupService(@ConfigProperty(name = "dodex.groups.checkForOwner") Boolean isCheckForOwner) {
        GroupService.isCheckForOwner = isCheckForOwner;
    }

    private final static DodexUtil dodexUtil = new DodexUtil();

    protected static boolean qmark = true;
    protected final static DateTimeFormatter formatter =
      DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());
    ZoneId zoneId = ZoneId.of("Europe/London");

    private final static EntityManager entityManager = dodexDatabase.getEntityManager();

    protected Group getGroupByName(String name, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Group> groupQuery = builder.createQuery(Group.class);
        Root<Group> root = groupQuery.from(Group.class);

        groupQuery.select(root).where(builder.equal(root.get(Group_.NAME), name));
        TypedQuery<Group> query = entityManager.createQuery(groupQuery);

        return query.getSingleResult();
    }

    protected User getUserByName(String name, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> userQuery = builder.createQuery(User.class);
        Root<User> root = userQuery.from(User.class);

        userQuery.select(root).where(builder.equal(root.get(User_.NAME), name));
        TypedQuery<User> query = entityManager.createQuery(userQuery);

        return query.getSingleResult();
    }

    public List<User> getMembersByGroup(Group group, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);

        Root<Group> groupRoot = criteriaQuery.from(Group.class);
        Join<Group, User> members = groupRoot.join(Group_.USERS);
        criteriaQuery.where(builder.equal(groupRoot.get(Group_.ID), group.getId()));

        return entityManager
          .createQuery(criteriaQuery.select(members))
          .getResultList();
    }

    public JsonObject addGroupAndMembers(JsonObject addGroupJson)
      throws IOException {
        if (dodexDatabase.getEntityManager() == null) {
            return addGroupJson;
        }
        EntityManager em = dodexDatabase.getEntityManager().getEntityManagerFactory().createEntityManager();

        final Map<String, String> selected = dodexUtil.commandMessage(addGroupJson.getString("groupMessage"));
        final List<String> selectedUsers = Arrays.asList(selected.get("selectedUsers").split(","));

        addGroupJson = addGroup(addGroupJson, em);

        String entry0 = selectedUsers.get(0);
        if (addGroupJson.getInt("status") == 0 &&
          entry0 != null && !entry0.isEmpty()) {
            try {
                addGroupJson = addMembers(em, selectedUsers, addGroupJson);
            } catch (SQLException | InterruptedException | IOException err) {
                err.printStackTrace();
                addGroupJson = Json.createBuilderFactory(Map.of())
                  .createObjectBuilder(addGroupJson)
                  .add("status", -1)
                  .add("errorMessage", err.getMessage()).build();
            }
        }

        em.close();
        return addGroupJson;
    }

    @Transactional
    protected JsonObject addGroup(JsonObject addGroupJson, EntityManager em)
      throws IOException {
        Timestamp current = new Timestamp(new Date().getTime());
        OffsetDateTime time = OffsetDateTime.now();
        Object currentDate = DbConfiguration.isUsingPostgres() ? time : current;
        LocalDateTime ldt = LocalDateTime.now();

        MessageUser messageUser = dodexDatabase.createMessageUser();
        messageUser.setName(addGroupJson.getString("groupOwner"));
        messageUser.setPassword(addGroupJson.getString("ownerId"));

        MessageUser messageUserSelected = dodexDatabase.selectUser(messageUser, null);

        try {
            Group group = getGroupByName(addGroupJson.getString("groupName"), em);
            if (group.getId() != 0) {
                addGroupJson = Json.createBuilderFactory(Map.of())
                  .createObjectBuilder(addGroupJson)
                  .add("ownerKey", messageUserSelected.getId())
                  .add("id", group.getId())
                  .add("status", 0)
                  .add("errorMessage", "").build();
            }
        } catch (NoResultException nre) {
            Group newGroup = new Group();
            newGroup.setName(addGroupJson.getString("groupName"));
            newGroup.setCreated(ldt);
            newGroup.setUpdated(ldt);
            newGroup.setOwner(messageUserSelected.getId().intValue());

            em.getTransaction().begin();
            em.persist(newGroup);
            em.getTransaction().commit();

            ZoneOffset preferredOffset = zoneId.getRules().getOffset(ldt);
            ZonedDateTime zonedDateTime = ZonedDateTime.ofLocal(ldt, zoneId, preferredOffset);

            String openApiDate = zonedDateTime.format(formatter);

            addGroupJson = Json.createBuilderFactory(Map.of())
              .createObjectBuilder(addGroupJson)
              .add("ownerKey", messageUserSelected.getId())
              .add("id", newGroup.getId())
              .add("status", 0)
              .add("created", openApiDate)
              .add("groupMessage", "Group added.")
              .add("errorMessage", "Group added.").build();
        }

        return addGroupJson;
    }

    @Transactional
    protected JsonObject addMembers(EntityManager em, List<String> selectedUsers, JsonObject addGroupJson)
      throws InterruptedException, SQLException, IOException {

        Group group = em.find(Group.class, addGroupJson.getInt("id"));
        JsonObject checkedJson = checkOnGroupOwner(addGroupJson, em);

        if (checkedJson.getBoolean("isValidForOperation")) {
            List<String> allUsers = new ArrayList<>();
            allUsers.add(addGroupJson.getString("groupOwner"));
            allUsers.addAll(selectedUsers);

            List<String> newMembers = checkOnMembers(group, allUsers, em);

            List<Long> userIds = new ArrayList<>();
            for (String memberName : newMembers) {
                User user = getUserByName(memberName, em);
                userIds.add(user.getId());
            }

            if (!userIds.isEmpty()) {
                em.getTransaction().begin();
                for (Long userId : userIds) {
                    Member member = new Member();
                    Member.MemberId memberId = new Member.MemberId(group.getId(), userId);
                    member.setMemberId(memberId);
                    em.persist(member);
                }
                em.getTransaction().commit();
            }
        } else {
            JsonObjectBuilder builder = Json.createBuilderFactory(Map.of())
              .createObjectBuilder(checkedJson);
            addGroupJson = builder.build();
        }

        return addGroupJson;
    }

    public JsonObject deleteGroupOrMembers(JsonObject deleteGroupJson)
      throws InterruptedException, SQLException, IOException {
        if (dodexDatabase.getEntityManager() == null) {
            return deleteGroupJson;
        }
        EntityManager em = dodexDatabase.getEntityManager().getEntityManagerFactory().createEntityManager();
        MessageUser messageUser = dodexDatabase.createMessageUser();

        Map<String, String> selected = dodexUtil.commandMessage(deleteGroupJson.getString("groupMessage"));
        final List<String> selectedUsers = Arrays.asList(selected.get("selectedUsers").split(","));

        messageUser.setName(deleteGroupJson.getString("groupOwner"));
        messageUser.setPassword(deleteGroupJson.getString("ownerId"));

        String entry0 = selectedUsers.get(0);

        if (deleteGroupJson.getInt("status") == 0 && "".equals(entry0)) {
            try {

                deleteGroupJson = deleteGroup(deleteGroupJson, em);

            } catch (Exception err) {
                errData(err, deleteGroupJson);
            }
        } else if (deleteGroupJson.getInt("status") == 0) {
            try {

                deleteGroupJson = deleteMembers(selectedUsers, deleteGroupJson, em);

            } catch (InterruptedException | SQLException | IOException err) {
                errData(err, deleteGroupJson);
            }
        }

        em.close();
        return deleteGroupJson;
    }

    @Transactional
    protected JsonObject deleteGroup(JsonObject deleteGroupJson, EntityManager em)
      throws InterruptedException, SQLException, IOException {

        JsonObject checkedJson = checkOnGroupOwner(deleteGroupJson, em);
        if (checkedJson.getBoolean("isValidForOperation")) {
            Group group = getGroupByName(deleteGroupJson.getString("groupName"), em);
            int deletedMembers = group.getMembers().size();

            em.getTransaction().begin();

            for (Member member : group.getMembers()) {
                em.remove(member);
            }
            em.flush();
            em.clear();

            group = getGroupByName(deleteGroupJson.getString("groupName"), em);

            em.remove(group);
            em.getTransaction().commit();

            String deleteMessage = null;
            if (deletedMembers > 0) {
                deleteMessage = deletedMembers + " members with group deleted";
            } else {
                deleteMessage = "Group " + group.getName() + " deleted.";
            }
            JsonObjectBuilder builder = Json.createBuilderFactory(Map.of())
              .createObjectBuilder(deleteGroupJson)
              .add("errorMessage", deleteMessage);
            deleteGroupJson = builder.build();
        } else {
            JsonObjectBuilder builder = Json.createBuilderFactory(Map.of())
              .createObjectBuilder(deleteGroupJson);
            builder
              .add("isValidForOperation", checkedJson.getBoolean("isValidForOperation"))
              .add("errorMessage", "Contact owner for group administration");

            deleteGroupJson = builder.build();
        }

        return deleteGroupJson;
    }

    @Transactional
    protected JsonObject deleteMembers(List<String> selectedUsers, JsonObject deleteGroupJson, EntityManager em)
      throws InterruptedException, SQLException, IOException {
        JsonObject checkedJson = checkOnGroupOwner(deleteGroupJson, em);
        if (checkedJson.getBoolean("isValidForOperation")) {
            Group group = getGroupByName(deleteGroupJson.getString("groupName"), em);
            deleteGroupJson = Json.createBuilderFactory(Map.of())
              .createObjectBuilder(deleteGroupJson).add("id", group.getId()).build();

            em.getTransaction().begin();

            for (Member member : group.getMembers()) {
                for (User user : group.getUsers()) {
                    if (selectedUsers.contains(user.getName())) {
                        if (user.getId() == member.getMemberId().getUser_id() &&
                          group.getId() == member.getMemberId().getGroup_id()) {
                            em.remove(member);
                        }
                    }
                }
            }
            em.getTransaction().commit();
            deleteGroupJson = Json.createBuilderFactory(Map.of())
              .createObjectBuilder(deleteGroupJson).add("groupMessage", "Group member(s) removed")
              .add("errorMessage", "Removed").build();
        } else {
            JsonObjectBuilder builder = Json.createBuilderFactory(Map.of())
              .createObjectBuilder(deleteGroupJson);
            builder
              .add("isValidForOperation", checkedJson.getBoolean("isValidForOperation"))
              .add("errorMessage", "Contact owner for group administration");
            deleteGroupJson = builder.build();
        }

        return deleteGroupJson;
    }

    public JsonObject getMembersList(JsonObject getGroupJson)
      throws InterruptedException, SQLException, IOException {
        if (dodexDatabase.getEntityManager() == null) {
            return getGroupJson;
        }
        EntityManager em = dodexDatabase.getEntityManager().getEntityManagerFactory().createEntityManager();
        MessageUser messageUser = dodexDatabase.createMessageUser();

        messageUser.setName(getGroupJson.getString("groupOwner"));
        messageUser.setPassword(getGroupJson.getString("ownerId"));

        messageUser = dodexDatabase.selectUser(messageUser, null);

        JsonArrayBuilder membersBuilder = Json.createArrayBuilder(Collections.EMPTY_LIST);
        JsonObjectBuilder builder = Json.createObjectBuilder(getGroupJson);
        builder = builder.add("ownerKey", messageUser.getId());

        try {
            Group group = getGroupByName(getGroupJson.getString("groupName"), em);
            List<User> users = getMembersByGroup(group, em);

            JsonObjectBuilder userBuilder = Json.createObjectBuilder(Map.of());

            if (!users.isEmpty()) {
                for (User user : users) {
                    if (!user.getName().equals(getGroupJson.getString("groupOwner"))) {
                        JsonObject userEntry = userBuilder.add("name", user.getName()).build();
                        membersBuilder = membersBuilder.add(userEntry);
                    }
                }
                getGroupJson = builder
                  .add("members", membersBuilder.build().toString())
                  .add("errorMessage", "")
                  .add("id", group.getId()).build();
            } else {
                getGroupJson = builder
                  .add("errorMessage", "Group not found: " + getGroupJson.getString("groupName"))
                  .add("id", 0).build();
            }
        } catch (NoResultException nre) {
            getGroupJson = builder
              .add("errorMessage", nre.getMessage())
              .add("groupMessage", "").build();
        }
        return getGroupJson;
    }

    protected JsonObject checkOnGroupOwner(JsonObject groupJson, EntityManager em) throws InterruptedException {
        Group group = getGroupByName(groupJson.getString("groupName"), em);
        User user = getUserByName(groupJson.getString("groupOwner"), em);
        JsonObjectBuilder builder = Json.createBuilderFactory(Map.of())
          .createObjectBuilder(groupJson);
        builder.add("isValidForOperation", false);

        if (group.getId() != 0) {
            groupJson = builder.add("id", group.getId())
              .add("ownerKey", group.getOwner()).build();
        }

        JsonObject checkGroupJson = builder
          .add("checkGroupOwnerId", user.getId())
          .add("checkGroupOwner", user.getName())
          .add("checkForOwner", isCheckForOwner != null && isCheckForOwner)
          .add("isValidForOperation", groupJson.getInt("status") != -1 &&
            !isCheckForOwner || user.getId() == groupJson.getInt("ownerKey"))
          .build();
        builder = Json.createBuilderFactory(Map.of())
          .createObjectBuilder(groupJson);

        builder
          .add("isValidForOperation", securityContext.isAuthenticated() ? securityContext.isAuthenticated() : checkGroupJson.getBoolean("isValidForOperation"))
          .add("errorMessage", !checkGroupJson.getBoolean("isValidForOperation") ?
            "Contact owner for group administration" : "");

        groupJson = builder.build();

        return groupJson;
    }

    protected List<String> checkOnMembers(Group group, List<String> selectedList, EntityManager em) {
        List<String> newSelected = new ArrayList<>(selectedList);

        for (String userName : selectedList) {
            Set<Member> members = group.getMembers();
            if (members == null) {
                return selectedList;
            }
            members.forEach(member -> {
                User user = getUserByName(userName, em);
                if (user.getId() == member.getMemberId().getUser_id()) {
                    newSelected.remove(userName);
                }
            });
        }

        return newSelected;
    }

    protected JsonObject errData(Throwable err, JsonObject groupJson) {
        if (err != null && err.getMessage() != null) {
            groupJson = Json.createBuilderFactory(Map.of())
              .createObjectBuilder(groupJson)
              .add("status", -1)
              .add("errorMessage", !err.getMessage().contains("batch execution") ?
                err.getMessage() : err.getMessage() + " -- some actions may have succeeded.")
              .build();
            if (!err.getMessage().contains("batch execution")) {
                err.printStackTrace();
            } else {
                logger.error(err.getMessage());
            }
        }
        return groupJson;
    }
}
