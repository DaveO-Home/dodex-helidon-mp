package dmo.fs.entities;

import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity(name = "Group")
@Table(name = "groups")
public class Group implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "groupsSeq", sequenceName = "groups_SEQ")
    private long id;

    @Basic(optional = false)
    @Column(name = "NAME", nullable = false)
    private String name;

    @Basic(optional = false)
    @Column(name = "OWNER", nullable = false)
    private Integer owner;

    @Basic(optional = false)
    @Column(name = "CREATED")
    private LocalDateTime created;

    @Basic(optional = false)
    @Column(name = "UPDATED")
    private LocalDateTime updated;

    @ManyToMany(cascade = {CascadeType.REMOVE}) //CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "member",
        joinColumns = {@JoinColumn(name = "GROUP_ID")},
        inverseJoinColumns = {@JoinColumn(name = "USER_ID")}
    )
    private Set<User> users = new HashSet<>(5);

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name="GROUP_ID")
    private Set<Member> members;

    public Set<Member> getMembers() {
        return members;
    }

    @Transient
    public Map<String, JsonValue> getMap() {
        Map<String, JsonValue> groupMap = new HashMap<>();
        groupMap.put("id", Json.createValue(this.id));
        groupMap.put("name", Json.createValue(this.name));
        groupMap.put("ownerId", Json.createValue(this.owner));
        groupMap.put("created", Json.createValue(this.created.toString()));
        groupMap.put("updated", Json.createValue(this.updated.toString()));
        return groupMap;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(Integer owner) {
        this.owner = owner;
    }

    public Integer getOwner() {
        return owner;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

}

