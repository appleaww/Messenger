package io.github.appleaww.messenger.model.dto.response;

import io.github.appleaww.messenger.model.dto.ParticipantDTO;

import java.util.List;

public record ChatCreateResponseDTO(
        Long id,
        String lastMessage,
        String companionName,
        List<ParticipantDTO> participantDTOList

) {}
