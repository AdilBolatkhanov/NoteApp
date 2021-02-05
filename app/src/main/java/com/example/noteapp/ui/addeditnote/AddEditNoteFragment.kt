package com.example.noteapp.ui.addeditnote

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.noteapp.R
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.ui.BaseFragment
import com.example.noteapp.ui.dialogs.ColorPickerDialogFragment
import com.example.noteapp.util.Constants.DEFAULT_NOTE_COLOR
import com.example.noteapp.util.Constants.KEY_LOGGED_IN_EMAIL
import com.example.noteapp.util.Constants.NO_EMAIL
import com.example.noteapp.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_edit_note.*
import java.util.*
import javax.inject.Inject

private const val FRAGMENT_TAG = "AddEditNoteFragment"

@AndroidEntryPoint
class AddEditNoteFragment : BaseFragment(R.layout.fragment_add_edit_note) {

    private val viewModel: AddEditNoteViewModel by viewModels()

    private val args: AddEditNoteFragmentArgs by navArgs()

    private var curNote: Note? = null
    private var curNoteColor = DEFAULT_NOTE_COLOR

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.id.isNotEmpty()) {
            viewModel.getNoteById(args.id)
            subscribeToObservers()
        }

        if (savedInstanceState != null) {
            val colorPickerDialog = parentFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                    as ColorPickerDialogFragment?
            colorPickerDialog?.setPositiveListener {
                changeViewNoteColor(it)
            }
        }

        viewNoteColor.setOnClickListener {
            ColorPickerDialogFragment().apply {
                setPositiveListener {
                    changeViewNoteColor(it)
                }
            }.show(parentFragmentManager, FRAGMENT_TAG)
        }
    }

    private fun changeViewNoteColor(colorStr: String) {
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.circle_shape, null)
        drawable?.let {
            val wrappedDrawable = DrawableCompat.wrap(it)
            val color = Color.parseColor("#$colorStr")
            DrawableCompat.setTint(wrappedDrawable, color)
            viewNoteColor.background = wrappedDrawable
            curNoteColor = colorStr
        }
    }

    private fun subscribeToObservers() {
        viewModel.note.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        val note = result.data!!
                        curNote = note
                        etNoteContent.setText(note.content)
                        etNoteTitle.setText(note.title)
                        changeViewNoteColor(note.color)
                    }
                    Status.ERROR -> {
                        showSnackbar(result.message ?: "Note not found")
                    }
                    Status.LOADING -> {
                        /* NO-OP */
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveNote()
    }

    private fun saveNote() {
        val authEmail = sharedPref.getString(KEY_LOGGED_IN_EMAIL, NO_EMAIL) ?: NO_EMAIL

        val title = etNoteTitle.text.toString()
        val content = etNoteContent.text.toString()
        if (title.isEmpty() || content.isEmpty())
            return
        val date = System.currentTimeMillis()
        val color = curNoteColor
        val id = curNote?.id ?: UUID.randomUUID().toString()
        val owners = curNote?.owners ?: listOf(authEmail)
        val note = Note(title, content, date, owners, color, id = id)
        viewModel.insertNote(note)
    }
}