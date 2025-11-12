package com.hereliesaz.julesapisdk.testapp

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.hereliesaz.julesapisdk.Source
import com.hereliesaz.julesapisdk.testapp.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var sourceAdapter: ArrayAdapter<String>
    private var sourcesList = listOf<Source>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        setupClickListeners()
        setupObservers()

        loadSettings()
    }

    private fun setupSpinner() {
        sourceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sourceSpinner.adapter = sourceAdapter
    }

    private fun setupClickListeners() {
        binding.getApiKeyButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jules.google.com/settings"))
            startActivity(intent)
        }

        binding.loadSourcesButton.setOnClickListener {
            val apiKey = binding.apiKeyEdittext.text.toString()
            if (apiKey.isNotBlank()) {
                viewModel.initializeClient(apiKey)
                viewModel.loadSources()
            } else {
                Toast.makeText(requireContext(), "Please enter an API key first.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.saveApiKeyButton.setOnClickListener {
            val selectedPosition = binding.sourceSpinner.selectedItemPosition
            if (selectedPosition >= 0 && sourcesList.isNotEmpty() && selectedPosition < sourcesList.size) {
                val selectedSource = sourcesList[selectedPosition]
                val apiKey = binding.apiKeyEdittext.text.toString()

                viewModel.createSession(selectedSource) // Create session with the selected source
                saveSettings(apiKey, selectedSource.name) // Save API key and the source NAME

                Toast.makeText(requireContext(), "Settings saved. Session being created in Chat tab.", Toast.LENGTH_LONG).show()
                // Switch to chat tab to see the result
                (requireActivity() as? MainActivity)?.binding?.viewPager?.currentItem = 0
            } else {
                Toast.makeText(requireContext(), "Please load and select a source.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        viewModel.sources.observe(viewLifecycleOwner) { sources ->
            sourcesList = sources
            val sourceDisplayNames = sources.map { it.url } // Display the user-friendly URL
            sourceAdapter.clear()
            sourceAdapter.addAll(sourceDisplayNames)
            sourceAdapter.notifyDataSetChanged()

            // Restore previously selected source
            val savedSourceName = getEncryptedSharedPreferences().getString("selected_source_name", null)
            if (savedSourceName != null) {
                val position = sourcesList.indexOfFirst { it.name == savedSourceName }
                if (position != -1) {
                    binding.sourceSpinner.setSelection(position)
                }
            }
        }

        viewModel.settingsError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_LONG).show()
                viewModel.clearSettingsError()
            }
        }
    }

    private fun getEncryptedSharedPreferences(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "JulesTestApp-Settings", // Use a unique name
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun saveSettings(apiKey: String, sourceName: String) {
        getEncryptedSharedPreferences().edit()
            .putString("api_key", apiKey)
            .putString("selected_source_name", sourceName)
            .apply()
    }

    private fun loadSettings() {
        val sharedPreferences = getEncryptedSharedPreferences()
        val apiKey = sharedPreferences.getString("api_key", "")
        if (!apiKey.isNullOrBlank()) {
            binding.apiKeyEdittext.setText(apiKey)
            viewModel.initializeClient(apiKey)
            viewModel.loadSources() // Auto-load sources if API key exists
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
