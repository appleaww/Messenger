package io.github.appleaww.messenger.model.dto;

import java.util.List;

public record ChatCreateResponseDTO(
        Long id,
        String lastMessage,
        List<ParticipantDTO> participantDTOList

) {}
