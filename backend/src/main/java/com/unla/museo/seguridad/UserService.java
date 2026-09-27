package com.unla.museo.seguridad;

import com.unla.museo.seguridad.dto.UserCreateRequest;
import com.unla.museo.seguridad.dto.PersonaTO;
import com.unla.museo.seguridad.dto.UserTO;

import java.util.List;


public interface UserService {

    UserTO create(UserCreateRequest request);

    boolean validateCredentials(String username, String rawPassword);

    UserTO getByEmail(String email);

    List<PersonaTO> getCuradores();

}
