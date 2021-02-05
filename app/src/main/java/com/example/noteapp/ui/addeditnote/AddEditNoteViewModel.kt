package com.example.noteapp.ui.addeditnote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.data.repositories.NoteRepository
import com.example.noteapp.util.Event
import com.example.noteapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _note = MutableLiveData<Event<Resource<Note>>>()
    val note: LiveData<Event<Resource<Note>>> = _note

    fun insertNote(note: Note) = GlobalScope.launch(Dispatchers.IO) {
        repository.insertNote(note)
    }

    fun getNoteById(id: String) = viewModelScope.launch {
        _note.postValue(Event(Resource.loading(null)))
        val note = repository.getNoteById(id)
        _note.postValue(Event(Resource.success(note)))
    }

}