package io.github.appleaww.messenger.controller;

import io.github.appleaww.messenger.model.dto.request.ChatCreateRequestDTO;
import io.github.appleaww.messenger.model.dto.response.ChatCreateResponseDTO;
import io.github.appleaww.messenger.model.dto.ChatDetailDTO;
import io.github.appleaww.messenger.model.dto.ChatListItemDTO;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/chats")
    public ResponseEntity<List<ChatListItemDTO>> showAllUserChats(@AuthenticationPrincipal User currentUser) {
        List<ChatListItemDTO> chats = chatService.getAllUserChatsWithDetails(currentUser);
        return ResponseEntity.ok(chats);
    }

    @PostMapping("/chats")
    public ResponseEntity<ChatCreateResponseDTO> createChat(
            @RequestBody ChatCreateRequestDTO chatCreateRequestDTO,
            @AuthenticationPrincipal User currentUser){
        return ResponseEntity.ok(chatService.createChat(chatCreateRequestDTO,currentUser));
    }

    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<List<ChatListItemDTO>> deleteChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal User currentUser){
        List<ChatListItemDTO> chats = chatService.deleteChat(chatId, currentUser);
        return ResponseEntity.ok(chats);
    }

    @PostMapping("/chats/{chatId}/close")
    public ResponseEntity<List<ChatListItemDTO>> closeChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal User currentUser){
        List<ChatListItemDTO> chats = chatService.closeCertainChat(chatId, currentUser);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/chats/{chatId}")
    public ResponseEntity<ChatDetailDTO> openChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal User user){
        return ResponseEntity.ok(chatService.openChat(chatId,user));
    }
}


