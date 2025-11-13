package io.github.appleaww.messenger.websocket;

import io.github.appleaww.messenger.model.dto.TypingDTO;
import io.github.appleaww.messenger.model.dto.request.MessageCreateRequestDTO;
import io.github.appleaww.messenger.model.dto.request.ReadReceiptRequestDTO;
import io.github.appleaww.messenger.model.dto.response.MessageCreateResponseDTO;
import io.github.appleaww.messenger.model.dto.response.ReadReceiptResponseDTO;
import io.github.appleaww.messenger.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            @Payload MessageCreateRequestDTO messageCreateRequestDTO
            ){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            log.error("No authentication found in SecurityContext");
            return;
        }

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();

        MessageCreateResponseDTO messageCreateResponseDTO = messageService.createMessage(messageCreateRequestDTO, userPrincipal.getUser());

        messagingTemplate.convertAndSendToUser(
                messageCreateResponseDTO.senderId().toString(),
                "/queue/chat-messages",
                messageCreateResponseDTO
        );

        messagingTemplate.convertAndSendToUser(
                messageCreateResponseDTO.recipientId().toString(),
                "/queue/chat-messages",
                messageCreateResponseDTO
        );

        log.debug("Message sent from User with id {} to User with id {}",
                messageCreateResponseDTO.senderId(), messageCreateResponseDTO.recipientId());
    }

    @MessageMapping("/chat.readMessages")
    public void markMessagesAsRead(
            @Payload ReadReceiptRequestDTO readReceiptRequestDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal){

        ReadReceiptResponseDTO readReceiptResponseDTO = messageService.processReadReceipt(readReceiptRequestDTO, userPrincipal.getUser());
        messagingTemplate.convertAndSendToUser(
                readReceiptResponseDTO.recipientId().toString(),
                "/queue/read-receipts",
                readReceiptResponseDTO
        );
        log.debug("User with id {} read messages in chat with id {} ",
                userPrincipal.getUser().getId(), readReceiptResponseDTO.chatId());

    }

    @MessageMapping("/chat.typing")
    public void displayTypingProcess(
            @Payload TypingDTO typingDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        TypingDTO typingResponseDTO = messageService.processTyping(typingDTO, userPrincipal.getUser());

        messagingTemplate.convertAndSendToUser(
                typingResponseDTO.recipientId().toString(),
                "/queue/typing-events",
                typingResponseDTO
        );
    }
}
