package com.github.devlucasjava.socialklyp.domain.policy;

import com.github.devlucasjava.socialklyp.domain.entity.Chat;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.valueobject.ChatCapacity;

/**
 * Domain Service for chat membership logic.
 * Encapsulates business rules for managing chat members and permissions.
 */
public interface ChatMembershipPolicy {

    /**
     * Checks if a profile can be added to the chat (capacity not full).
     * @param chat the chat
     * @return true if there's room for another member
     */
    boolean canAddMember(Chat chat);

    /**
     * Checks if adding the specified profile would exceed capacity.
     * @param chat the chat
     * @param profile the profile to add
     * @return true if profile can be added, false if chat is full
     */
    boolean canAddProfile(Chat chat, Profile profile);

    /**
     * Validates that profile is not already a member.
     * @param chat the chat
     * @param profile the profile to check
     * @return true if profile is not already a member
     */
    boolean isNotAlreadyMember(Chat chat, Profile profile);

    /**
     * Gets the current chat capacity.
     * @param chat the chat
     * @return ChatCapacity value object
     */
    ChatCapacity getCapacity(Chat chat);

    /**
     * Checks if profile is an admin of the chat.
     * @param chat the chat
     * @param profile the profile to check
     * @return true if profile is an admin or creator
     */
    boolean isAdmin(Chat chat, Profile profile);

    /**
     * Checks if profile is the chat creator.
     * @param chat the chat
     * @param profile the profile to check
     * @return true if profile created the chat
     */
    boolean isCreator(Chat chat, Profile profile);

    /**
     * Checks if profile is a member (including admin and creator).
     * @param chat the chat
     * @param profile the profile to check
     * @return true if profile is a member
     */
    boolean isMember(Chat chat, Profile profile);
}
