package com.example.noteapp.ui.notes

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.noteapp.R
import com.example.noteapp.ui.BaseFragment
import com.example.noteapp.util.Constants.KEY_LOGGED_IN_EMAIL
import com.example.noteapp.util.Constants.KEY_LOGGED_IN_PASSWORD
import com.example.noteapp.util.Constants.NO_EMAIL
import com.example.noteapp.util.Constants.NO_PASSWORD
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_notes.*
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment: BaseFragment(R.layout.fragment_notes) {

    @Inject
    lateinit var sharedPref: SharedPreferences

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
        fabAddNote.setOnClickListener {
            findNavController().navigate(
                NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(
                    ""
                )
            )
        }
    }

    private fun logout() {
        sharedPref.edit().putString(KEY_LOGGED_IN_EMAIL, NO_EMAIL).apply()
        sharedPref.edit().putString(KEY_LOGGED_IN_PASSWORD, NO_PASSWORD).apply()
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.notesFragment, true)
            .build()
        findNavController().navigate(
            NotesFragmentDirections.actionNotesFragmentToAuthFragment(),
            navOptions
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miLogout -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_notes, menu)
    }
}