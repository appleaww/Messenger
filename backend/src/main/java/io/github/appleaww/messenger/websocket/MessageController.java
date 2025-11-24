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
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            @Payload MessageCreateRequestDTO messageCreateRequestDTO,
            Principal principal){

        if(principal == null){
            log.error("User principal is null");
            return;
        }

        Authentication authentication = (Authentication) principal;
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

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
            Principal principal){
        if (principal == null) {
            log.error("Principal is null");
            return;
        }
        Authentication authentication = (Authentication) principal;
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

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
            Principal principal){
        Authentication authentication = (Authentication) principal;
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        TypingDTO typingResponseDTO = messageService.processTyping(typingDTO, userPrincipal.getUser());

        messagingTemplate.convertAndSendToUser(
                typingResponseDTO.recipientId().toString(),
                "/queue/typing-events",
                typingResponseDTO
        );
    }
}
