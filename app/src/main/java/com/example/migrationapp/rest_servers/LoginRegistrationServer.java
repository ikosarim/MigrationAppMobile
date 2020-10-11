package com.example.migrationapp.rest_servers;

import com.example.migrationapp.dto.LoginDto;
import com.example.migrationapp.dto.RegisterDto;
import com.example.migrationapp.dto.Token;
import com.example.migrationapp.dto.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginRegistrationServer {

    @POST("/auth/register")
    Call<Token> registration(@Body RegisterDto registerDto);

    @POST("/auth/login")
    Call<User> login(@Body LoginDto loginDto);
}
