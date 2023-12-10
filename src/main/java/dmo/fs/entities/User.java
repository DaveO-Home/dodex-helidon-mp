package dmo.fs.entities;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "User")
@Table(name = "users")

public class User implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  @Id
  @Column(name = "ID", nullable = false, updatable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @SequenceGenerator(name="usersSeq", sequenceName="users_SEQ")
  private long id;

  @Basic(optional = false)
  @Column(name = "NAME", nullable = false)
  private String name;

  @Basic(optional = false)
  @Column(name = "PASSWORD", nullable = false)
  private String password;

  @Basic(optional = false)
  @Column(name = "IP", nullable = false)
  private String ip;

  @Basic(optional = false)
  @Column(name = "LAST_LOGIN")
  private LocalDateTime last_login;

  @ManyToMany(cascade = { CascadeType.ALL }, fetch=FetchType.LAZY) //CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(
      name = "undelivered",
      joinColumns = { @JoinColumn(name = "USER_ID") },
      inverseJoinColumns = { @JoinColumn(name = "MESSAGE_ID") }
  )

  private Set<Message> messages = new HashSet<>(5);

  public Set<Message> getMessages() {
    return messages;
  }

  public void setMessages(Set<Message> messages) {
    this.messages = messages;
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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public LocalDateTime getLastLogin() {
    return last_login;
  }

  public void setLastLogin(LocalDateTime last_login) {
    this.last_login = last_login;
  }
}
