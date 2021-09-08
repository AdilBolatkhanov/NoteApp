package com.example.noteapp.data.repositories

import android.app.Application
import com.example.noteapp.data.local.NoteDao
import com.example.noteapp.data.local.entities.LocallyDeletedNoteId
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.data.remote.NoteApi
import com.example.noteapp.data.remote.requests.AccountRequest
import com.example.noteapp.data.remote.requests.AddOwnerRequest
import com.example.noteapp.data.remote.requests.DeleteNoteRequest
import com.example.noteapp.util.Resource
import com.example.noteapp.util.checkForInternetConnection
import com.example.noteapp.util.networkBoundResource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject
import kotlin.system.measureTimeMillis

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

    suspend fun deleteNote(noteId: String) = coroutineScope {
        val response = try {
            noteApi.deleteNote(DeleteNoteRequest(noteId))
        } catch (e: Exception) {
            null
        }
        launch {
            noteDao.deleteNoteById(noteId)
        }
        if (response == null || !response.isSuccessful) {
            noteDao.insertLocallyDeletedNoteId(LocallyDeletedNoteId(noteId))
        } else {
            deleteLocallyDeletedNoteId(noteId)
        }
    }

    suspend fun deleteLocallyDeletedNoteId(deletedNoteId: String) {
        noteDao.deleteLocallyDeletedNoteId(deletedNoteId)
    }

    private suspend fun insertNotes(notes: List<Note>) = coroutineScope {
        val insertAllNotesJob = notes.map { launch { insertNote(it) } }
        insertAllNotesJob.joinAll()
    }

    suspend fun getNoteById(noteId: String) = noteDao.getNoteById(noteId)

    private var curNotesResponse: Response<List<Note>>? = null

    private suspend fun syncNotes() = coroutineScope {
        val time = measureTimeMillis {
            val locallyDeletedNoteIds = noteDao.getAllLocallyDeletedNoteIDs()
            val deleteLocallyDeletedNotesJobs = locallyDeletedNoteIds.map { id ->
                launch {
                    deleteNote(id.deletedNoteId)
                }
            }
            deleteLocallyDeletedNotesJobs.joinAll()

            val unsyncedNotes = noteDao.getAllUnsyncedNotes()
            val insertUnsyncedNotes = unsyncedNotes.map { note -> launch { insertNote(note) } }
            insertUnsyncedNotes.joinAll()

            curNotesResponse = noteApi.getNotes()
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

    suspend fun addOwnerToNote(owner: String, noteId: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.addOwnerToNote(AddOwnerRequest(noteId, owner))
            if (response.isSuccessful && response.body()!!.successful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch (e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your connection", null)
        }
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