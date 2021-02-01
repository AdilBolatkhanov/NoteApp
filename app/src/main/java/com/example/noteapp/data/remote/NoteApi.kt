package com.example.noteapp.data.remote

import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.data.remote.requests.AccountRequest
import com.example.noteapp.data.remote.requests.AddOwnerRequest
import com.example.noteapp.data.remote.requests.DeleteNoteRequest
import com.example.noteapp.data.remote.responses.SimpleResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface NoteApi {

    @POST("/register")
    suspend fun register(
        @Body registerRequest: AccountRequest
    ): Response<SimpleResponse>

    @POST("/login")
    suspend fun login(
        @Body loginRequest: AccountRequest
    ): Response<SimpleResponse>

    @POST("/note/add")
    suspend fun addNote(
        @Body note: Note
    ): Response<ResponseBody>

    @DELETE("/note/delete")
    suspend fun deleteNote(
        @Body deleteNoteRequest: DeleteNoteRequest
    ): Response<ResponseBody>

    @POST("/note/addOwner")
    suspend fun addOwnerToNote(
        @Body addOwnerRequest: AddOwnerRequest
    ): Response<SimpleResponse>

    @GET("/note/getAll")
    suspend fun getNotes(): Response<List<Note>>
}