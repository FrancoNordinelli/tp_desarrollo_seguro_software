package com.unla.museo.services;

import com.unla.museo.dto.request.UserCreateRequest;
import com.unla.museo.dto.to.PersonaTO;
import com.unla.museo.dto.to.UserTO;

import java.util.List;


public interface UserService {

    UserTO create(UserCreateRequest request);

    boolean validateCredentials(String username, String rawPassword);

    UserTO getByEmail(String email);

    List<PersonaTO> getCuradores();

}
