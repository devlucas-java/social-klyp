package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.response.link.LinkResponse;
import com.github.devlucasjava.socialklyp.application.mapper.LinkMapper;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ConflictException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ForbiddenException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.UnauthorizeException;
import com.github.devlucasjava.socialklyp.domain.entity.Chat;
import com.github.devlucasjava.socialklyp.domain.entity.Link;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ChatRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.LinkRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final LinkMapper linkMapper;
    private final ChatRepository chatRepository;

    @Transactional
    public LinkResponse createLink(User auth, UUID chatId, LocalDateTime expirationDate) {
        User user = getUserOrThrow(auth.getId());
        Chat chat = getChatOrThrow(chatId);

        validateAdmin(user.getProfile(), chat);

        Link link = new Link();
        link.setChat(chat);
        link.setActive(true);
        link.setExpirationDate(expirationDate);
        link.setProfile(user.getProfile());
        link.setUsersSubscribed(0);

        return linkMapper.toResponse(linkRepository.save(link));
    }

    @Transactional
    public void deactivateLink(User auth, UUID linkId) {
        User user = getUserOrThrow(auth.getId());
        Link link = getLinkOrThrow(linkId);

        validateAdmin(user.getProfile(), link.getChat());

        link.setActive(false);
        linkRepository.save(link);
    }

    @Transactional
    public void subscribe(User auth, UUID linkId) {
        User user = getUserOrThrow(auth.getId());
        Link link = getLinkOrThrow(linkId);

        if (!link.isActive()) {
            throw new ForbiddenException("This invite link is no longer active");
        }

        if (link.isExpired()) {
            throw new ForbiddenException("This invite link has expired");
        }

        Chat chat = link.getChat();
        Profile profile = user.getProfile();

        if (chat.isMember(profile)) {
            throw new ConflictException("You are already a member of this chat");
        }

        if (chat.isFull()) {
            throw new ConflictException("Chat has reached the maximum limit of " + Chat.MAX_MEMBERS + " members");
        }

        chat.addMemberProfile(profile);
        link.setUsersSubscribed(link.getUsersSubscribed() + 1);

        chatRepository.save(chat);
        linkRepository.save(link);
    }

    private Chat getChatOrThrow(UUID id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found with id: " + id));
    }

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Link getLinkOrThrow(UUID id) {
        return linkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found with id: " + id));
    }

    private void validateAdmin(Profile profile, Chat chat) {
        if (!chat.isAdmin(profile)) {
            throw new UnauthorizeException("You are not an admin of this chat");
        }
    }
}

