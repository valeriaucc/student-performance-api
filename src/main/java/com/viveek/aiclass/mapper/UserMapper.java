package com.viveek.aiclass.mapper;

import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.dto.request.CreateUserRequest;
import com.viveek.aiclass.dto.request.UpdateUserRequest;
import com.viveek.aiclass.dto.response.UserResponse;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entity conversions.
 * Handles mapping between User entities and DTOs with automatic field mapping.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Maps User entity to UserResponse DTO.
     *
     * @param user the User entity
     * @return UserResponse DTO
     */
    UserResponse toResponse(User user);

    /**
     * Maps CreateUserRequest to User entity.
     *
     * @param request the CreateUserRequest DTO
     * @return User entity
     */
    User toEntity(CreateUserRequest request);

    /**
     * Updates existing User entity from UpdateUserRequest.
     * Null values in the request are ignored.
     *
     * @param request the UpdateUserRequest DTO
     * @param user the existing User entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);
}

