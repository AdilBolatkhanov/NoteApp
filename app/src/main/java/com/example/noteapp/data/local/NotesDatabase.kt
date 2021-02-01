package com.example.noteapp.data.local

import androidx.room.Database
import androidx.room.TypeConverters
import com.example.noteapp.data.local.entities.Note

@Database(
    entities = [Note::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class NotesDatabase {
    abstract fun noteDao() : NoteDao
}