package com.hereliesaz.julesapisdk.testapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.net.toUri
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

        setupRecyclerView()
        setupClickListeners()
        setupObservers()

        loadSettings()
    }

    private fun setupRecyclerView() {
        sourcesAdapter = SourcesAdapter { source ->
            // Optional: Handle source selection directly, e.g., for immediate feedback
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

        binding.apiKeyEdittext.addTextChangedListener(object : android.text.TextWatcher {
            private var searchFor: String = ""
            private val handler = android.os.Handler(android.os.Looper.getMainLooper())
            private val runnable = Runnable {
                val apiKey = searchFor
                if (apiKey.isNotBlank()) {
                    viewModel.initializeClient(apiKey)
                    viewModel.loadSources()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchFor = s.toString()
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 500) // 500ms debounce
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.saveApiKeyButton.setOnClickListener {
            val selectedSource = sourcesAdapter.getSelectedSource()
            if (selectedSource != null) {
                val apiKey = binding.apiKeyEdittext.text.toString()

                viewModel.initializeClient(apiKey)
                viewModel.addLog("Settings saved. Requesting session creation...")
                viewModel.createSession(selectedSource)
                saveSettings(apiKey, selectedSource.name)

            } else {
                viewModel.addLog("Save failed: Please load and select a source first.")
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.SourcesLoaded -> {
                        sourcesAdapter.submitList(state.sources)
                        val savedSourceName = getEncryptedSharedPreferences().getString("selected_source_name", null)
                        if (savedSourceName != null) {
                            sourcesAdapter.setSelectedSource(savedSourceName)
                        }
                        binding.progressBar.visibility = View.GONE
                    }
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Error -> {
                        // You can show a toast or a snackbar here
                        binding.progressBar.visibility = View.GONE
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
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
            putString("selected_source_name", sourceName)
        }
    }

    private fun loadSettings() {
        val sharedPreferences = getEncryptedSharedPreferences()
        val apiKey = sharedPreferences.getString("api_key", "")
        if (!apiKey.isNullOrBlank()) {
            binding.apiKeyEdittext.setText(apiKey)
            viewModel.initializeClient(apiKey)
            viewModel.loadSources()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
