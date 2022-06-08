package com.aditasha.myapplication

sealed class Result<out R> private constructor() {
    data class Success<out T>(val data: T? = null) : Result<T>()
    data class Error(val error: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}