package com.github.devlucasjava.socialklyp.application.mapper;

import com.github.devlucasjava.socialklyp.application.dto.request.auth.RegisterDTO;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileSummary;
import com.github.devlucasjava.socialklyp.application.dto.response.user.UserDTO;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserMapper {

    private final ProfileMapper profileMapper;

    public UserDTO toDTO(User user) {
        ProfileSummary profile = profileMapper.toSummary(user.getProfile());
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .birthDay(user.getBirthDay())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .business(user.isBusiness())
                .roles(user.getRoles())
                .profile(profile)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toEntity(RegisterDTO registerDTO) {
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setFirstName(registerDTO.getFirstName());
        user.setLastName(registerDTO.getLastName());
        user.setUsername(registerDTO.getUsername());
        user.setBirthDay(registerDTO.getBirthDay());
        user.setEmail(registerDTO.getEmail());
        user.setEmailVerified(false);
        user.setBusiness(false);
        return user;
    }
}
