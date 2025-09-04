package io.github.appleaww.messenger.controller;

import io.github.appleaww.messenger.model.dto.ChatCreateRequestDTO;
import io.github.appleaww.messenger.model.dto.ChatCreateResponseDTO;
import io.github.appleaww.messenger.model.entity.Chat;
import io.github.appleaww.messenger.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private ChatService chatService;

    @GetMapping("/chats")
    public List<Chat> getAllChats() {
        return chatService.getAllChats();
    }

    @GetMapping("/chats/{id}")
    public Optional<Chat> getChatById(
            @PathVariable("id") Long id){
        return chatService.getChatById(id);
    }

    @PostMapping("/chats")
    public ChatCreateResponseDTO createChat(
            @RequestBody ChatCreateRequestDTO chatCreateRequestDTO){
        return chatService.createChat(chatCreateRequestDTO);
    }
}
