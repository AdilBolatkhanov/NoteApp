package com.example.noteapp.data.repositories

import android.app.Application
import com.example.noteapp.data.local.NoteDao
import com.example.noteapp.data.remote.NoteApi
import com.example.noteapp.data.remote.requests.AccountRequest
import com.example.noteapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteApi: NoteApi,
    private val context: Application
) {

    suspend fun register(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.register(AccountRequest(email, password))
            if (response.isSuccessful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.message(), null)
            }
        } catch (e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your connection", null)
        }
    }

}