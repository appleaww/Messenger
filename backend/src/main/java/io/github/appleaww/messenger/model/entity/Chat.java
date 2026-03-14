package io.github.appleaww.messenger.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "chats", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"participants", "messages"})
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return Objects.equals(id, chat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
