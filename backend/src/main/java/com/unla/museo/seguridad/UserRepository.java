package com.unla.museo.seguridad;

import java.util.List;
import java.util.Optional;


public interface UserRepository{

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    UserEntity save(UserEntity userEntity);

    Optional<UserEntity> findById(Long id);

    List<UserEntity> findByRoleIdOrderByFirstNameAscLastNameAsc(String roleId);

}
