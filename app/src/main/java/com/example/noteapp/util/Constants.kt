package com.example.noteapp.util

object Constants {

    val IGNORE_AUTH_URLS = listOf("/register", "/login")

    const val KEY_LOGGED_IN_EMAIL = "KEY_LOGGED_IN_EMAIL"
    const val KEY_LOGGED_IN_PASSWORD = "KEY_LOGGED_IN_PASSWORD"

    const val NO_EMAIL = "NO_EMAIL"
    const val NO_PASSWORD = "NO_PASSWORD"

    const val DEFAULT_NOTE_COLOR = "FFA500"

    const val DATABASE_NAME = "notes-database"

    const val BASE_URL = "http://192.168.100.2:8080"

    const val ENCRYPTED_SHARED_PREF_NAME = "enc_shared_pref"

}