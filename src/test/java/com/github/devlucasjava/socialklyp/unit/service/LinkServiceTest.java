package com.github.devlucasjava.socialklyp.unit.service;

import com.github.devlucasjava.socialklyp.application.dto.response.link.LinkResponse;
import com.github.devlucasjava.socialklyp.application.mapper.LinkMapper;
import com.github.devlucasjava.socialklyp.application.service.LinkService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @InjectMocks
    private LinkService linkService;

    @Mock private LinkRepository linkRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private LinkMapper linkMapper;

    private User authUser;
    private Profile adminProfile;
    private Profile memberProfile;
    private Profile outsiderProfile;
    private Chat chat;
    private Link activeLink;
    private LinkResponse linkResponse;

    private final UUID userId      = UUID.randomUUID();
    private final UUID chatId      = UUID.randomUUID();
    private final UUID linkId      = UUID.randomUUID();
    private final UUID adminId     = UUID.randomUUID();
    private final UUID memberId    = UUID.randomUUID();
    private final UUID outsiderId  = UUID.randomUUID();

    @BeforeEach
    void setup() {
        authUser = new User();
        authUser.setId(userId);

        adminProfile = new Profile();
        adminProfile.setId(adminId);

        memberProfile = new Profile();
        memberProfile.setId(memberId);

        outsiderProfile = new Profile();
        outsiderProfile.setId(outsiderId);

        chat = new Chat();
        chat.setId(chatId);
        chat.setName("Dev Team");
        chat.setProfileCreator(adminProfile);          // creator = admin
        chat.setProfiles(new HashSet<>(Set.of(memberProfile)));
        chat.setProfilesAdmin(new HashSet<>());

        activeLink = new Link();
        activeLink.setId(linkId);
        activeLink.setActive(true);
        activeLink.setChat(chat);
        activeLink.setProfile(adminProfile);
        activeLink.setUsersSubscribed(0);
        activeLink.setExpirationDate(null);            // sem expiração

        linkResponse = LinkResponse.builder()
                .id(linkId)
                .chatId(chatId)
                .active(true)
                .usersSubscribed(0)
                .build();

        // LinkService.createLink / deactivateLink usam getUserOrThrow(auth.getId())
        authUser.setProfile(adminProfile);
    }

    // -------------------- CREATE LINK --------------------

    @Test
    void shouldCreateLinkSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(authUser));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(linkRepository.save(any(Link.class))).thenReturn(activeLink);
        when(linkMapper.toResponse(activeLink)).thenReturn(linkResponse);

        LinkResponse result = linkService.createLink(authUser, chatId, null);

        assertNotNull(result);
        assertEquals(linkId, result.getId());
        verify(linkRepository).save(any(Link.class));
    }

    @Test
    void shouldCreateLinkWithExpirationDate() {
        LocalDateTime expiration = LocalDateTime.now().plusDays(7);

        when(userRepository.findById(userId)).thenReturn(Optional.of(authUser));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(linkRepository.save(any(Link.class))).thenReturn(activeLink);
        when(linkMapper.toResponse(activeLink)).thenReturn(linkResponse);

        LinkResponse result = linkService.createLink(authUser, chatId, expiration);

        assertNotNull(result);
        verify(linkRepository).save(any(Link.class));
    }

    @Test
    void shouldThrowWhenNonAdminCreatesLink() {
        User nonAdminUser = new User();
        nonAdminUser.setId(UUID.randomUUID());
        nonAdminUser.setProfile(outsiderProfile);

        when(userRepository.findById(nonAdminUser.getId())).thenReturn(Optional.of(nonAdminUser));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        assertThrows(UnauthorizeException.class,
                () -> linkService.createLink(nonAdminUser, chatId, null));

        verify(linkRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenChatNotFoundOnCreateLink() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(authUser));
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> linkService.createLink(authUser, chatId, null));
    }

    @Test
    void shouldThrowWhenUserNotFoundOnCreateLink() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> linkService.createLink(authUser, chatId, null));
    }

    // -------------------- DEACTIVATE LINK --------------------

    @Test
    void shouldDeactivateLinkSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(authUser));
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(activeLink));
        when(linkRepository.save(activeLink)).thenReturn(activeLink);

        linkService.deactivateLink(authUser, linkId);

        assertFalse(activeLink.isActive());
        verify(linkRepository).save(activeLink);
    }

    @Test
    void shouldThrowWhenNonAdminDeactivatesLink() {
        User nonAdminUser = new User();
        nonAdminUser.setId(UUID.randomUUID());
        nonAdminUser.setProfile(outsiderProfile);

        when(userRepository.findById(nonAdminUser.getId())).thenReturn(Optional.of(nonAdminUser));
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(activeLink));

        assertThrows(UnauthorizeException.class,
                () -> linkService.deactivateLink(nonAdminUser, linkId));

        verify(linkRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenLinkNotFoundOnDeactivate() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(authUser));
        when(linkRepository.findById(linkId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> linkService.deactivateLink(authUser, linkId));
    }

    // -------------------- SUBSCRIBE --------------------

    @Test
    void shouldSubscribeSuccessfully() {
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setProfile(outsiderProfile);

        when(userRepository.findById(newUser.getId())).thenReturn(Optional.of(newUser));
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(activeLink));
        when(chatRepository.save(chat)).thenReturn(chat);
        when(linkRepository.save(activeLink)).thenReturn(activeLink);

        linkService.subscribe(newUser, linkId);

        assertTrue(chat.getProfiles().contains(outsiderProfile));
        assertEquals(1, activeLink.getUsersSubscribed());
        verify(chatRepository).save(chat);
        verify(linkRepository).save(activeLink);
    }

    @Test
    void shouldIncrementUsersSubscribedOnEachSubscribe() {
        activeLink.setUsersSubscribed(5);

        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setProfile(outsiderProfile);

        when(userRepository.findById(newUser.getId())).thenReturn(Optional.of(newUser));
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(activeLink));
        when(chatRepository.save(chat)).thenReturn(chat);
        when(linkRepository.save(activeLink)).thenReturn(activeLink);

        linkService.subscribe(newUser, linkId);

        assertEquals(6, activeLink.getUsersSubscribed());
    }

    @Test
    void shouldThrowWhenLinkIsInactive() {
        activeLink.setActive(false);

        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setProfile(outsiderProfile);

        when(userRepository.findById(newUser.getId())).thenReturn(Optional.of(newUser));
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(activeLink));

        assertThrows(ForbiddenException.class,
                () -> linkService.subscribe(newUser, linkId));

        verify(chatRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenLinkIsExpired() {
        activeLink.setExpirationDate(LocalDateTime.now().minusDays(1)); // expirado

        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setProfile(outsiderProfile);

        when(userRepository.findById(newUser.getId())).thenReturn(Optional.of(newUser));
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(activeLink));

        assertThrows(ForbiddenException.class,
                () -> linkService.subscribe(newUser, linkId));

        verify(chatRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAlreadyMemberOnSubscribe() {
        User memberUser = new User();
        memberUser.setId(UUID.randomUUID());
        memberUser.setProfile(memberProfile); // já é membro

        when(userRepository.findById(memberUser.getId())).thenReturn(Optional.of(memberUser));
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(activeLink));

        assertThrows(ConflictException.class,
                () -> linkService.subscribe(memberUser, linkId));

        verify(chatRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenChatIsFullOnSubscribe() {
        Set<Profile> bigSet = new HashSet<>();
        for (int i = 0; i < Chat.MAX_MEMBERS; i++) {
            Profile p = new Profile();
            p.setId(UUID.randomUUID());
            bigSet.add(p);
        }
        chat.setProfiles(bigSet);

        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setProfile(outsiderProfile);

        when(userRepository.findById(newUser.getId())).thenReturn(Optional.of(newUser));
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(activeLink));

        assertThrows(ConflictException.class,
                () -> linkService.subscribe(newUser, linkId));

        verify(chatRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenLinkNotFoundOnSubscribe() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(authUser));
        when(linkRepository.findById(linkId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> linkService.subscribe(authUser, linkId));
    }

    @Test
    void shouldNotExpireWhenExpirationDateIsInFuture() {
        activeLink.setExpirationDate(LocalDateTime.now().plusDays(7)); // válido

        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setProfile(outsiderProfile);

        when(userRepository.findById(newUser.getId())).thenReturn(Optional.of(newUser));
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(activeLink));
        when(chatRepository.save(chat)).thenReturn(chat);
        when(linkRepository.save(activeLink)).thenReturn(activeLink);

        assertDoesNotThrow(() -> linkService.subscribe(newUser, linkId));
    }
}
