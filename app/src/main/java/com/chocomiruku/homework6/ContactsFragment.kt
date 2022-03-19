package com.chocomiruku.homework6

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chocomiruku.homework6.databinding.FragmentContactsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar


const val readContactsPermission = Manifest.permission.READ_CONTACTS

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ContactNameAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)

        binding.getContactsButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    readContactsPermission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    retrieveContactsNames()
                }
                shouldShowRequestPermissionRationale(readContactsPermission) -> {
                    showRationale()
                }
                else -> {
                    requestPermissionLauncher.launch(readContactsPermission)
                }
            }
        }

        adapter = ContactNameAdapter()
        binding.contactsList.adapter = adapter

        return binding.root
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                retrieveContactsNames()
            } else {
                showSnackBarCannotRetrieveContacts()
            }
        }

    private fun retrieveContactsNames() {
        val contactNames = mutableListOf<String>()
        val resolver = requireContext().contentResolver
        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null, null
        )

        cursor?.let {
            val nameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameColumnIndex)
                    contactNames.add(name)
                }
                showContactsList(contactNames)
            } else {
                showSnackBarNoContactsFound()
            }

            cursor.close()
        }
    }

    private fun showContactsList(contactNames: List<String>) {
        adapter.submitList(contactNames)
        binding.emptyListText.visibility = View.GONE
        binding.getContactsButton.visibility = View.GONE
    }

    private fun showRationale() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.title))
            .setMessage(resources.getString(R.string.supporting_text))
            .setIcon(R.drawable.ic_pleading_face)
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_CONTACTS
                )
            }
            .show()
    }

    private fun showSnackBarNoContactsFound() {
        Snackbar.make(binding.getContactsButton, R.string.no_contacts_found, Snackbar.LENGTH_LONG)
            .show()
    }

    private fun showSnackBarCannotRetrieveContacts() {
        Snackbar.make(binding.getContactsButton, R.string.no_permission, Snackbar.LENGTH_LONG)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}