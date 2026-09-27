package com.unla.museo.seguridad;

import java.util.Optional;

public interface RoleRepository {
    Optional<RoleEntity> findById(String id);
    RoleEntity save(RoleEntity role);
}