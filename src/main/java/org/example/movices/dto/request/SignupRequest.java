package org.example.movices.dto.request;

import lombok.Data;
import org.example.movices.model.entity.Role;

import java.util.Set;

@Data
public class SignupRequest {
    private String username;
    private String email;
    private String password;
    private Set<String> Role;

}
