package com.example.noteapp.ui.notedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.repositories.NoteRepository
import com.example.noteapp.util.Event
import com.example.noteapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _addOwnerStatus = MutableLiveData<Event<Resource<String>>>()

    val addOwnerStatus: LiveData<Event<Resource<String>>> = _addOwnerStatus

    fun addOwnerToNote(owner: String, noteID: String) {
        _addOwnerStatus.postValue(Event(Resource.loading(null)))
        if (owner.isEmpty() || noteID.isEmpty()) {
            _addOwnerStatus.postValue(Event(Resource.error("The owner can't be empty", null)))
        }

        viewModelScope.launch {
            val result = repository.addOwnerToNote(owner, noteID)
            _addOwnerStatus.postValue(Event(result))
        }
    }

    fun observeNoteById(noteID: String) = repository.observeNoteById(noteID)
}