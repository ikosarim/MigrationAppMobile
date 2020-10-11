package com.example.migrationapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterDto {

    private String phone;
    private String userName;
    private String nationality;
    private String dateOfBirth;
    private String password;
}
