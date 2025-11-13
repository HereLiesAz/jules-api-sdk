package com.hereliesaz.julesapisdk.testapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hereliesaz.julesapisdk.testapp.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var sessionsAdapter: SessionsAdapter
    private lateinit var sourcesAdapter: SourcesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupClickListeners()
        setupObservers()

        loadSettings()
    }

    private fun setupRecyclerViews() {
        // Setup Sessions Adapter
        // *** MODIFIED: The adapter now handles Session, and passes it to resumeSession ***
        sessionsAdapter = SessionsAdapter { session ->
            viewModel.resumeSession(session)
            (activity as? MainActivity)?.binding?.viewPager?.currentItem = 0 // Switch to chat
        }
        binding.sessionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sessionsAdapter
        }

        // Setup Sources Adapter
        sourcesAdapter = SourcesAdapter { source ->
            viewModel.createSession(source)
            (activity as? MainActivity)?.binding?.viewPager?.currentItem = 0 // Switch to chat
        }
        binding.sourcesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sourcesAdapter
        }
    }

    private fun setupClickListeners() {
        binding.getApiKeyButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://jules.google.com/settings".toUri())
            startActivity(intent)
        }

        binding.loadDataButton.setOnClickListener {
            val apiKey = binding.apiKeyEdittext.text.toString()
            if (apiKey.isNotBlank()) {
                viewModel.initializeClient(apiKey)
                viewModel.loadSettingsData() // Call the new combined function
                saveSettings(apiKey, "") // Save the key on load
            } else {
                Toast.makeText(requireContext(), "Please enter an API Key", Toast.LENGTH_SHORT).show()
                viewModel.addLog("Load failed: API Key is blank.")
            }
        }

        binding.saveApiKeyButton.setOnClickListener {
            val apiKey = binding.apiKeyEdittext.text.toString()
            if(apiKey.isNotBlank()) {
                saveSettings(apiKey, "") // Save just the API key
                Toast.makeText(requireContext(), "API Key saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "API Key is blank", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setListsVisibility(isVisible: Boolean) {
        binding.sessionsRecyclerView.isVisible = isVisible
        binding.sourcesRecyclerView.isVisible = isVisible
        binding.sessionsLabel.isVisible = isVisible
        binding.sourcesLabel.isVisible = isVisible
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state is UiState.Loading

                when (state) {
                    is UiState.Idle -> {
                        setListsVisibility(false)
                    }
                    is UiState.Loading -> {
                        setListsVisibility(false)
                    }
                    is UiState.SettingsLoaded -> {
                        setListsVisibility(true)
                        sessionsAdapter.submitList(state.sessions)
                        sourcesAdapter.submitList(state.sources)
                    }
                    is UiState.Error -> {
                        setListsVisibility(false)
                        Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    }
                    // Handle deprecated states just in case
                    is UiState.SessionsLoaded -> {
                        setListsVisibility(true)
                        binding.sourcesRecyclerView.isVisible = false // Hide other list
                        binding.sourcesLabel.isVisible = false
                        sessionsAdapter.submitList(state.sessions)
                    }
                    is UiState.SourcesLoaded -> {
                        setListsVisibility(true)
                        binding.sessionsRecyclerView.isVisible = false // Hide other list
                        binding.sessionsLabel.isVisible = false
                        sourcesAdapter.submitList(state.sources)
                    }
                }
            }
        }
    }

    private fun getEncryptedSharedPreferences(): SharedPreferences {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        val masterKey = MasterKey.Builder(requireContext())
            .setKeyGenParameterSpec(keyGenParameterSpec)
            .build()

        return EncryptedSharedPreferences.create(
            requireContext(),
            "JulesTestApp-Settings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun saveSettings(apiKey: String, sourceName: String) {
        getEncryptedSharedPreferences().edit {
            putString("api_key", apiKey)
            // We no longer save the selected source name
            // putString("selected_source_name", sourceName)
        }
    }

    private fun loadSettings() {
        val sharedPreferences = getEncryptedSharedPreferences()
        val apiKey = sharedPreferences.getString("api_key", "")
        if (!apiKey.isNullOrBlank()) {
            binding.apiKeyEdittext.setText(apiKey)
            viewModel.initializeClient(apiKey)
            // Auto-load data on start
            viewModel.loadSettingsData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}