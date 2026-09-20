package com.warnes.erp.modules.security.mapper;

import com.warnes.erp.modules.security.dto.request.UserCreateRequestDTO;
import com.warnes.erp.modules.security.dto.response.UserResponseDTO;
import com.warnes.erp.modules.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponseDTO toResponseDTO(User entity);

    // La contraseña no se mapea directamente porque debe ser encriptada por el Service
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(UserCreateRequestDTO dto);
}