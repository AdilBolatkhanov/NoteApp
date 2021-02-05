package com.example.noteapp.data.repositories

import android.app.Application
import com.example.noteapp.data.local.NoteDao
import com.example.noteapp.data.local.entities.LocallyDeletedNoteId
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.data.remote.NoteApi
import com.example.noteapp.data.remote.requests.AccountRequest
import com.example.noteapp.data.remote.requests.DeleteNoteRequest
import com.example.noteapp.util.Resource
import com.example.noteapp.util.checkForInternetConnection
import com.example.noteapp.util.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteApi: NoteApi,
    private val context: Application
) {

    suspend fun insertNote(note: Note) {
        val response = try {
            noteApi.addNote(note)
        } catch (e: Exception) {
            null
        }
        if (response != null && response.isSuccessful)
            noteDao.insertNote(note.apply { isSynced = true })
        else
            noteDao.insertNote(note)
    }

    suspend fun deleteNote(noteId: String) {
        val response = try {
            noteApi.deleteNote(DeleteNoteRequest(noteId))
        } catch (e: Exception) {
            null
        }
        noteDao.deleteNoteById(noteId)
        if (response == null || !response.isSuccessful) {
            noteDao.insertLocallyDeletedNoteId(LocallyDeletedNoteId(noteId))
        } else {
            deleteLocallyDeletedNoteId(noteId)
        }
    }

    suspend fun deleteLocallyDeletedNoteId(deletedNoteId: String) {
        noteDao.deleteLocallyDeletedNoteId(deletedNoteId)
    }

    private suspend fun insertNotes(notes: List<Note>) {
        notes.forEach { insertNote(it) }
    }

    suspend fun getNoteById(noteId: String) = noteDao.getNoteById(noteId)

    private var curNotesResponse: Response<List<Note>>? = null

    suspend fun syncNotes() {
        val locallyDeletedNoteIds = noteDao.getAllLocallyDeletedNoteIDs()
        locallyDeletedNoteIds.forEach { id -> deleteNote(id.deletedNoteId) }

        val unsyncedNotes = noteDao.getAllUnsyncedNotes()
        unsyncedNotes.forEach { note -> insertNote(note) }

        curNotesResponse = noteApi.getNotes()
        curNotesResponse?.body()?.let { notes ->
            noteDao.deleteAllNotes()
            insertNotes(notes.onEach { note -> note.isSynced = true })
        }
    }

    fun observeNoteById(noteId: String) = noteDao.observeNoteById(noteId)

    fun getAllNotes(): Flow<Resource<List<Note>>> {
        return networkBoundResource(
            query = {
                noteDao.getAllNotes()
            },
            fetch = {
                syncNotes()
                curNotesResponse
            },
            saveFetchResult = { response ->
                response?.body()?.let {
                    insertNotes(it)
                }
            },
            shouldFetch = {
                checkForInternetConnection(context)
            }
        )
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.login(AccountRequest(email, password))
            if (response.isSuccessful && response.body()!!.successful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch (e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your connection", null)
        }
    }

    suspend fun register(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.register(AccountRequest(email, password))
            if (response.isSuccessful && response.body()!!.successful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch (e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your connection", null)
        }
    }

}