package com.aditasha.myapplication.api

data class Login(
    val email: String,
    val password: String
)

data class Register(
    val name: String,
    val email: String,
    val password: String
)