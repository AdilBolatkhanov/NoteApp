package com.example.noteapp.ui.notedetail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteapp.R
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.ui.BaseFragment
import com.example.noteapp.ui.dialogs.AddOwnerDialog
import com.example.noteapp.util.Event
import com.example.noteapp.util.Resource
import com.example.noteapp.util.Status
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.fragment_note_detail.*

private const val ADD_OWNER_TAG = "ADD_OWNER_TAG"

@AndroidEntryPoint
class NoteDetailFragment : BaseFragment(R.layout.fragment_note_detail) {

    private val viewModel: NoteDetailViewModel by viewModels()

    private val args: NoteDetailFragmentArgs by navArgs()

    private var curNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupClickListeners()

        restoreDialog(savedInstanceState)
    }

    private fun restoreDialog(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val addOwnerDialog = parentFragmentManager.findFragmentByTag(ADD_OWNER_TAG)
                    as AddOwnerDialog?
            addOwnerDialog?.setPositiveListener {
                addOwnerToCurNote(it)
            }
        }
    }

    private fun setupClickListeners() {
        fabEditNote.setOnClickListener {
            findNavController().navigate(
                NoteDetailFragmentDirections.actionNoteDetailFragmentToAddEditNoteFragment(args.id)
            )
        }
    }

    private fun showAddOwnerDialog() {
        AddOwnerDialog().apply {
            setPositiveListener {
                addOwnerToCurNote(it)
            }
        }.show(parentFragmentManager, ADD_OWNER_TAG)
    }

    private fun addOwnerToCurNote(email: String) {
        curNote?.let { note ->
            viewModel.addOwnerToNote(email, note.id)
        }
    }

    private fun setMarkdownText(text: String) {
        val markwon = Markwon.create(requireContext())
        val markdown = markwon.toMarkdown(text)
        markwon.setParsedMarkdown(tvNoteContent, markdown)
    }

    private fun subscribeToObservers() {
        viewModel.addOwnerStatus.observe(viewLifecycleOwner, ::handleAddOwnerEvent)
        viewModel.observeNoteById(args.id).observe(viewLifecycleOwner, ::handleObserveNoteByIdEvent)
    }

    private fun handleAddOwnerEvent(event: Event<Resource<String>>?) {
        event?.getContentIfNotHandled()?.let { result ->
            when (result.status) {
                Status.LOADING -> {
                    addOwnerProgressBar.visibility = View.VISIBLE
                    showSnackbar(result.data ?: "Successfully added owner to note")
                }
                Status.ERROR -> {
                    addOwnerProgressBar.visibility = View.GONE
                    showSnackbar(result.message ?: "An unknown error occured")
                }
                Status.SUCCESS -> {
                    addOwnerProgressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun handleObserveNoteByIdEvent(note: Note?) {
        note?.let { notes ->
            tvNoteTitle.text = notes.title
            setMarkdownText(notes.content)
            curNote = notes
        } ?: showSnackbar("Note not found")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.note_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miAddOwner -> showAddOwnerDialog()
        }
        return super.onOptionsItemSelected(item)
    }
}