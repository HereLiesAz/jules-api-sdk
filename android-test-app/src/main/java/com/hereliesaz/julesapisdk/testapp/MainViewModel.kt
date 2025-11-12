package com.hereliesaz.julesapisdk.testapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.julesapisdk.CreateSessionRequest
import com.hereliesaz.julesapisdk.JulesClient
import com.hereliesaz.julesapisdk.JulesSession
import com.hereliesaz.julesapisdk.Source
import com.hereliesaz.julesapisdk.SourceContext
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter

class MainViewModel : ViewModel() {

    // For Chat
    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    // For Settings
    private val _sources = MutableLiveData<List<Source>>()
    val sources: LiveData<List<Source>> = _sources
    private val _settingsError = MutableLiveData<String?>()
    val settingsError: LiveData<String?> = _settingsError

    private var julesClient: JulesClient? = null
    private var julesSession: JulesSession? = null

    // Called from SettingsFragment when API key is available
    fun initializeClient(apiKey: String) {
        if (apiKey.isNotBlank()) {
            julesClient = JulesClient(apiKey)
        }
    }

    // Called from SettingsFragment when "Load Sources" is clicked
    fun loadSources() {
        if (julesClient == null) {
            _settingsError.postValue("API Key is not set.")
            return
        }
        viewModelScope.launch {
            try {
                val sourceList = julesClient?.listSources()?.sources
                if (sourceList.isNullOrEmpty()) {
                    _settingsError.postValue("No sources found for this API key.")
                } else {
                    _sources.postValue(sourceList!!)
                }
            } catch (e: Exception) {
                _settingsError.postValue("Error loading sources: ${e.message}")
            }
        }
    }

    // Called from SettingsFragment when "Save" is clicked
    fun createSession(source: Source) {
        if (julesClient == null) {
            addMessage(Message("API Key not set. Please configure in Settings.", MessageType.ERROR))
            return
        }
        viewModelScope.launch {
            try {
                _messages.postValue(emptyList()) // Clear chat on new session
                julesSession = julesClient?.createSession(CreateSessionRequest("Test Application", SourceContext(source.name)))
                addMessage(Message("Session created with source: ${source.name}", MessageType.BOT))
            } catch (e: Exception) {
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                val stackTraceString = sw.toString()
                addMessage(Message("Error creating session:\n$stackTraceString", MessageType.ERROR))
            }
        }
    }


    fun sendMessage(text: String) {
        if (julesSession == null) {
            addMessage(Message("Session not created. Please configure API Key and Source in Settings.", MessageType.ERROR))
            return
        }

        addMessage(Message(text, MessageType.USER))

        viewModelScope.launch {
            try {
                val response = julesSession?.sendMessage(text)
                response?.let {
                    addMessage(Message(it.message, MessageType.BOT))
                }
            } catch (e: Exception) {
                addMessage(Message("Error sending message: ${e.message}", MessageType.ERROR))
            }
        }
    }

    private fun addMessage(message: Message) {
        val newMessages = _messages.value.orEmpty().toMutableList()
        newMessages.add(message)
        _messages.postValue(newMessages)
    }

    fun clearSettingsError() {
        _settingsError.value = null
    }
}
