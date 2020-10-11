package com.example.migrationapp.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    @SerializedName("phone")
    @Expose
    private String phone;

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("nationality")
    @Expose
    private String nationality;

    @SerializedName("dateOfBirth")
    @Expose
    private Date dateOfBirth;
}
