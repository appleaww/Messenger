package io.github.appleaww.messenger.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chats", schema = "public")
@Data
@NoArgsConstructor
@ToString(exclude = {"participants", "messages"})
@EqualsAndHashCode(exclude = {"participants", "messages"})
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_message", columnDefinition = "TEXT DEFAULT 'Send the first message!'", nullable = false)
    private String lastMessage;

    @ManyToMany(mappedBy = "chats")
    Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Message> messages = new HashSet<>();
}
