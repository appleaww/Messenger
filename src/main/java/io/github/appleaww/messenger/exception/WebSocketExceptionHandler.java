package io.github.appleaww.messenger.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class WebSocketExceptionHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(
            Message<byte[]> clientMessage,
            Throwable exception) {

        log.error("WebSocket error: {}", exception.getMessage(), exception);

        if (exception.getCause() instanceof IllegalArgumentException) {
            return handleAuthenticationError(clientMessage, exception.getCause().getMessage());
        }

        return super.handleClientMessageProcessingError(clientMessage, exception);
    }

    private Message<byte[]> handleAuthenticationError(Message<byte[]> clientMessage, String errorMessage) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);

        accessor.setMessage(errorMessage);
        accessor.setLeaveMutable(true);

        String errorPayload = "Authentication failed: " + errorMessage;

        return MessageBuilder.createMessage(
                errorPayload.getBytes(StandardCharsets.UTF_8),
                accessor.getMessageHeaders()
        );
    }
}