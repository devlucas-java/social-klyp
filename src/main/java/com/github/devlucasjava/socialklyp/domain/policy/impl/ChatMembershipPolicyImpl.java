package com.github.devlucasjava.socialklyp.domain.policy.impl;

import com.github.devlucasjava.socialklyp.delivery.rest.advice.ConflictException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Chat;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.policy.ChatMembershipPolicy;
import com.github.devlucasjava.socialklyp.domain.valueobject.ChatCapacity;
import org.springframework.stereotype.Service;

@Service
public class ChatMembershipPolicyImpl implements ChatMembershipPolicy {

    @Override
    public boolean canAddMember(Chat chat) {

        validateChat(chat);

        if (chat.isFull()) {
            throw new ConflictException("Chat is full");
        }

        return true;
    }

    @Override
    public boolean canAddProfile(Chat chat, Profile profile) {

        validateChat(chat);
        validateProfile(profile);

        if (chat.isFull()) {
            throw new ConflictException("Chat is full");
        }

        if (chat.isMember(profile)) {
            throw new ConflictException("Profile is already a member");
        }

        return true;
    }

    @Override
    public boolean isNotAlreadyMember(Chat chat, Profile profile) {

        validateChat(chat);
        validateProfile(profile);

        if (chat.isMember(profile)) {
            throw new ConflictException("Profile is already a member");
        }

        return true;
    }

    @Override
    public ChatCapacity getCapacity(Chat chat) {

        validateChat(chat);

        return ChatCapacity.of(chat.memberCount());
    }

    @Override
    public boolean isAdmin(Chat chat, Profile profile) {

        validateChat(chat);
        validateProfile(profile);

        return chat.isAdmin(profile);
    }

    @Override
    public boolean isCreator(Chat chat, Profile profile) {

        validateChat(chat);
        validateProfile(profile);

        return chat.isCreator(profile);
    }

    @Override
    public boolean isMember(Chat chat, Profile profile) {

        validateChat(chat);
        validateProfile(profile);

        return chat.isMember(profile);
    }

    private void validateChat(Chat chat) {
        if (chat == null) {
            throw new ResourceNotFoundException("Chat not found");
        }
    }

    private void validateProfile(Profile profile) {
        if (profile == null) {
            throw new ResourceNotFoundException("Profile not found");
        }
    }
}