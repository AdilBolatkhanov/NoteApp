package com.example.noteapp.data.remote.requests

data class AddOwnerRequest(
    val noteId: String,
    val owner: String
)
