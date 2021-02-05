package com.example.noteapp.ui.notedetail

import androidx.lifecycle.ViewModel
import com.example.noteapp.data.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    fun observeNoteById(noteID: String) = repository.observeNoteById(noteID)
}