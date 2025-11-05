package io.github.appleaww.messenger.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

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

    @ElementCollection
    @CollectionTable(
            name = "chat_names",
            joinColumns = @JoinColumn(name = "chat_id")
    )
    @MapKeyColumn(name = "user_id")
    @Column(name = "chat_name")
    private Map<Long, String> chatNames = new HashMap<>();

    @ManyToMany
    @JoinTable(
            name = "user_chats",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sendingTime ASC") //сортировка по времени отправки по ascending (возрастанию)
    private List<Message> messages = new ArrayList<>();
}
