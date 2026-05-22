package com.github.devlucasjava.socialklyp.unit.policy;

import com.github.devlucasjava.socialklyp.domain.entity.Chat;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.policy.impl.ChatMembershipPolicyImpl;
import com.github.devlucasjava.socialklyp.domain.valueobject.ChatCapacity;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ChatMembershipPolicyImplTest {

    private final ChatMembershipPolicyImpl policy = new ChatMembershipPolicyImpl();

    @Test
    void shouldNotAllowNullChat() {
        assertFalse(policy.canAddMember(null));
        assertThrows(IllegalArgumentException.class, () -> policy.getCapacity(null));
    }

    @Test
    void shouldAllowAddingProfileWhenChatHasRoomAndProfileNotMember() {
        Chat chat = createChat();
        Profile profile = createProfile("member-1");

        assertTrue(policy.canAddProfile(chat, profile));
    }

    @Test
    void shouldNotAllowAddingProfileWhenAlreadyMember() {
        Chat chat = createChat();
        Profile profile = createProfile("member-1");
        chat.addMemberProfile(profile);

        assertFalse(policy.canAddProfile(chat, profile));
        assertFalse(policy.isNotAlreadyMember(chat, profile));
    }

    @Test
    void shouldReturnCorrectCapacity() {
        Chat chat = createChat();
        chat.addMemberProfile(createProfile("member-1"));
        chat.addAdminProfile(createProfile("admin-1"));

        ChatCapacity capacity = policy.getCapacity(chat);

        assertEquals(2, capacity.count());
        assertFalse(capacity.isFull());
        assertEquals(48, capacity.remainingCapacity());
    }

    @Test
    void shouldIdentifyAdminCreatorAndMemberRoles() {
        Profile creator = createProfile("creator");
        Profile admin = createProfile("admin");
        Profile member = createProfile("member");

        Chat chat = createChat();
        chat.setProfileCreator(creator);
        chat.addAdminProfile(admin);
        chat.addMemberProfile(member);

        assertTrue(policy.isCreator(chat, creator));
        assertTrue(policy.isAdmin(chat, admin));
        assertTrue(policy.isAdmin(chat, creator));
        assertTrue(policy.isMember(chat, member));
        assertTrue(policy.isMember(chat, creator));
        assertFalse(policy.isCreator(chat, member));
    }

    private Chat createChat() {
        Chat chat = new Chat();
        chat.setProfiles(new HashSet<>());
        chat.setProfilesAdmin(new HashSet<>());
        return chat;
    }

    private Profile createProfile(String idSuffix) {
        Profile profile = new Profile();
        profile.setId(java.util.UUID.nameUUIDFromBytes(idSuffix.getBytes()));
        profile.setDisplayName(idSuffix);
        profile.setBio("Bio");
        return profile;
    }
}
