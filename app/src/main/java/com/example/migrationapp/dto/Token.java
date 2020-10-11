package com.example.migrationapp.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Token {

    @SerializedName("text")
    @Expose
    private String text;
}
