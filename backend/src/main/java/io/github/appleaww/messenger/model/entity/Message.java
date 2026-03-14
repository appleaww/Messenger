package io.github.appleaww.messenger.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "messages", schema = "public")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sending_time", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private Instant sendingTime;

    @Column(name = "message_content")
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User sender;

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message)o;
        return Objects.equals(id, message.id);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(id);
    }
}
