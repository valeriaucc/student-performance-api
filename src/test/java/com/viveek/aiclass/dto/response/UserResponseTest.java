package com.viveek.aiclass.dto.response;

import com.viveek.aiclass.domain.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

    @Test
    void builder_CreatesUserResponse() {
        UUID id = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();
        ZonedDateTime now = ZonedDateTime.now();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");
        
        UserResponse response = UserResponse.builder()
                .id(id)
                .authUserId(authUserId)
                .fullName("John Doe")
                .email("john@example.com")
                .role(UserRole.STUDENT)
                .metadata(metadata)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getAuthUserId()).isEqualTo(authUserId);
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(response.getMetadata()).containsEntry("key", "value");
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void allArgsConstructor_CreatesUserResponse() {
        UUID id = UUID.randomUUID();
        ZonedDateTime now = ZonedDateTime.now();
        
        UserResponse response = new UserResponse(
                id, UUID.randomUUID(), "Jane Doe", "jane@example.com",
                UserRole.TEACHER, null, now, now
        );
        
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getFullName()).isEqualTo("Jane Doe");
        assertThat(response.getRole()).isEqualTo(UserRole.TEACHER);
    }

    @Test
    void noArgsConstructor_CreatesEmptyResponse() {
        UserResponse response = new UserResponse();
        assertThat(response).isNotNull();
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        UserResponse response = new UserResponse();
        UUID id = UUID.randomUUID();
        
        response.setId(id);
        response.setFullName("Test User");
        response.setEmail("test@test.com");
        response.setRole(UserRole.STUDENT);
        
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getFullName()).isEqualTo("Test User");
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getRole()).isEqualTo(UserRole.STUDENT);
    }
}


