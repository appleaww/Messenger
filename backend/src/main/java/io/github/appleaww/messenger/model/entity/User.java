package io.github.appleaww.messenger.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"chats", "messages"})
@EqualsAndHashCode(exclude = {"chats", "messages"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "username", unique = true, length = 50, nullable = false)
    private String username;

    @Column(name = "email", unique = true, length = 100, nullable = false)
    private String email;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    @Column(name = "last_seen")
    private Instant lastSeen;

    @ManyToMany(mappedBy = "participants")
    private Set<Chat> chats = new HashSet<>();

    @OneToMany(mappedBy = "sender")
    private Set<Message> messages = new HashSet<>();

    public enum Role {
        ADMIN, USER
    }
}
