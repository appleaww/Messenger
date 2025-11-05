package io.github.appleaww.messenger.model.dto;

import java.util.List;

public record ChatCreateResponseDTO(
        Long id,
        String lastMessage,
        String companionName,
        List<ParticipantDTO> participantDTOList

) {}
