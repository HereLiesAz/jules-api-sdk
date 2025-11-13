package com.hereliesaz.julesapisdk.testapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.julesapisdk.Activity
import com.hereliesaz.julesapisdk.ApiConfig
import com.hereliesaz.julesapisdk.CreateSessionRequest
import com.hereliesaz.julesapisdk.GithubRepoSource
import com.hereliesaz.julesapisdk.GithubRepoContext
// *** MODIFIED: Renamed import ***
import com.hereliesaz.julesapisdk.JulesApiClient
import com.hereliesaz.julesapisdk.JulesSession
import com.hereliesaz.julesapisdk.SdkResult
import com.hereliesaz.julesapisdk.Session
import com.hereliesaz.julesapisdk.Source
import com.hereliesaz.julesapisdk.SourceContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {

    // A single StateFlow to hold the UI state
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    // For Chat tab
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    // For Logcat tab
    private val _diagnosticLogs = MutableStateFlow<List<String>>(emptyList())
    val diagnosticLogs: StateFlow<List<String>> = _diagnosticLogs

    // *** MODIFIED: Renamed property type ***
    private var julesClient: JulesApiClient? = null
    private var julesSession: JulesSession? = null

    // State to track if the session is active
    private var isSessionActive = false

    fun addLog(log: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val currentLogs = _diagnosticLogs.value.toMutableList()
        currentLogs.add("$timestamp: $log")
        _diagnosticLogs.value = currentLogs
    }

    fun initializeClient(apiKey: String) {
        if (apiKey.isNotBlank()) {
            // *** MODIFIED: Construct with ApiConfig ***
            julesClient = JulesApiClient(ApiConfig(apiKey))
            addLog("JulesApiClient initialized.")
        } else {
            addLog("Attempted to initialize client with blank API key.")
        }
    }

    fun loadSettingsData() {
        if (julesClient == null) {
            _uiState.value = UiState.Error("API Key is not set. Cannot load data.")
            return
        }
        addLog("Loading sessions and sources...")
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Run both calls in parallel
                val (sessionsResult, sourcesResult) = coroutineScope {
                    val sessionsJob = async { julesClient!!.listSessions() }
                    val sourcesJob = async { julesClient!!.listSources() }
                    Pair(sessionsJob.await(), sourcesJob.await())
                }

                // Check results
                val sessions = when (sessionsResult) {
                    is SdkResult.Success -> sessionsResult.data.sessions ?: emptyList()
                    is SdkResult.Error -> throw Exception("Sessions Error: ${sessionsResult.code} - ${sessionsResult.body}")
                    is SdkResult.NetworkError -> throw Exception("Sessions Network Error: ${sessionsResult.throwable.message}")
                }

                val sources = when (sourcesResult) {
                    is SdkResult.Success -> sourcesResult.data.sources ?: emptyList()
                    is SdkResult.Error -> throw Exception("Sources Error: ${sourcesResult.code} - ${sourcesResult.body}")
                    is SdkResult.NetworkError -> throw Exception("Sources Network Error: ${sourcesResult.throwable.message}")
                }

                // Emit the new combined state
                _uiState.value = UiState.SettingsLoaded(sessions, sources)
                addLog("Successfully loaded ${sessions.size} sessions and ${sources.size} sources.")

            } catch (e: Exception) {
                val errorMsg = "Error loading settings data: ${e.message}"
                _uiState.value = UiState.Error(errorMsg)
                addLog(errorMsg)
            }
        }
    }

    // *** MODIFIED: This function now takes a Session and is much simpler ***
    fun resumeSession(session: Session) {
        if (julesClient == null) {
            addLog("Error: Client not initialized. Cannot resume session.")
            return
        }
        addLog("Resuming session: ${session.name}...")
        viewModelScope.launch {
            _messages.value = emptyList() // Clear chat

            // Since listSessions() now returns full Session objects, we can just wrap it.
            // No need for another network call.
            julesSession = JulesSession(julesClient!!, session)
            isSessionActive = (session.state != "COMPLETED" && session.state != "FAILED")

            val successMsg = "Resumed session: ${session.name}\nState: ${session.state ?: "UNKNOWN"}"
            addMessage(Message(successMsg, MessageType.BOT))
            addLog(successMsg)

            _uiState.value = UiState.Idle // Hide spinner
            loadActivities() // Load the history
        }
    }

    // This function is called when clicking a new source
    fun createSession(source: Source) {
        if (julesClient == null) {
            addLog("Error: API Key is not set. Cannot create session.")
            return
        }
        addLog("Creating session with source: ${source.name}")
        viewModelScope.launch {
            _messages.value = emptyList() // Clear chat on new session
            val sourceContext = if (source is GithubRepoSource) {
                SourceContext(source.name, GithubRepoContext("main"))
            } else {
                SourceContext(source.name)
            }
            _uiState.value = UiState.Loading
            when (val result = julesClient?.createSession(CreateSessionRequest("Test Application", sourceContext))) {
                is SdkResult.Success -> {
                    julesSession = result.data
                    isSessionActive = true
                    if (source is GithubRepoSource) {
                        val url = "https://github.com/${source.githubRepo.owner}/${source.githubRepo.repo}"
                        val successMsg = "Session created with source: $url"
                        addMessage(Message(successMsg, MessageType.BOT))
                        addLog(successMsg)
                    } else {
                        addLog("Session created with source: ${source.name} (URL not available)")
                    }
                    // We don't need to change UI state, just load activities
                    _uiState.value = UiState.Idle
                    loadActivities()
                }
                is SdkResult.Error -> {
                    val errorMsg = "API Error creating session: ${result.code} - ${result.body}"
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    addLog(errorMsg)
                    isSessionActive = false
                    _uiState.value = UiState.Error(errorMsg)
                }
                is SdkResult.NetworkError -> {
                    val sw = StringWriter()
                    result.throwable.printStackTrace(PrintWriter(sw))
                    val errorMsg = "Network error creating session:$sw"
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    addLog(errorMsg)
                    isSessionActive = false
                    _uiState.value = UiState.Error(errorMsg)
                }
                null -> {
                    addLog("Error: JulesApiClient is not initialized.")
                    _uiState.value = UiState.Error("JulesApiClient is not initialized.")
                }
            }
        }
    }

    // --- DEPRECATED FUNCTIONS (no longer called) ---
    fun loadSessions() {
        addLog("WARNING: 'loadSessions' is deprecated. Use 'loadSettingsData'.")
    }
    fun loadSources() {
        addLog("WARNING: 'loadSources' is deprecated. Use 'loadSettingsData'.")
    }


    private fun loadActivities() {
        if (julesSession == null) {
            addLog("Cannot load activities: Session not initialized.")
            return
        }
        addLog("Loading activities...")
        viewModelScope.launch {
            when (val result = julesSession?.listActivities()) {
                is SdkResult.Success -> {
                    val newMessages = mutableListOf<Message>()
                    val activities = result.data.activities ?: emptyList()

                    for (activity in activities) {
                        when (activity) {
                            is Activity.UserMessagedActivity -> newMessages.add(Message(activity.userMessaged.userMessage, MessageType.USER))
                            is Activity.AgentMessagedActivity -> newMessages.add(Message(activity.agentMessaged.agentMessage, MessageType.BOT))
                            is Activity.PlanGeneratedActivity -> {
                                val planText = "Plan Generated:\n" + activity.planGenerated.plan.steps?.joinToString("\n") { "  ${it.index + 1}. ${it.title}" }
                                newMessages.add(Message(planText, MessageType.BOT))
                            }
                            is Activity.ProgressUpdatedActivity -> newMessages.add(Message("[BOT: ${activity.progressUpdated.title}]", MessageType.BOT))
                            is Activity.SessionCompletedActivity -> {
                                newMessages.add(Message("[BOT: Session Completed]", MessageType.BOT))
                                isSessionActive = false
                            }
                            is Activity.SessionFailedActivity -> {
                                newMessages.add(Message("[BOT: Session Failed - ${activity.sessionFailed.reason}]", MessageType.ERROR))
                                isSessionActive = false
                            }
                            is Activity.PlanApprovedActivity -> newMessages.add(Message("[USER: Plan Approved]", MessageType.USER))
                            // Handle unknown activities gracefully
                            else -> addLog("Ignored unknown activity.")
                        }
                    }
                    _messages.value = newMessages
                    addLog("Successfully loaded ${activities.size} activities.")
                }
                is SdkResult.Error -> {
                    val errorMsg = "API Error loading activities: ${result.code} - ${result.body}"
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    addLog(errorMsg)
                    isSessionActive = false
                }
                is SdkResult.NetworkError -> {
                    val sw = StringWriter()
                    result.throwable.printStackTrace(PrintWriter(sw))
                    val errorMsg = "Network error loading activities:$sw"
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    addLog(errorMsg)
                    isSessionActive = false
                }
                null -> {}
            }
        }
    }

    fun sendMessage(text: String) {
        if (julesSession == null) {
            val errorMsg = "Session not created. Please configure API Key and Source in Settings."
            addMessage(Message(errorMsg, MessageType.ERROR))
            addLog(errorMsg)
            return
        }

        if (!isSessionActive) {
            val errorMsg = "Session is closed. Please create a new session in Settings to continue."
            addMessage(Message(errorMsg, MessageType.ERROR))
            addLog(errorMsg)
            return
        }

        addLog("Sending message: $text")

        viewModelScope.launch {
            when (val result = julesSession?.sendMessage(text)) {
                is SdkResult.Success -> {
                    addLog("Message sent successfully. Refreshing activities...")
                    loadActivities()
                }
                is SdkResult.Error -> {
                    val errorMsg = "Error sending message: ${result.code} - ${result.body}"
                    addLog(errorMsg)
                    addMessage(Message(errorMsg, MessageType.ERROR))
                    if (result.code == 404) isSessionActive = false
                }
                is SdkResult.NetworkError -> {
                    val sw = StringWriter()
                    result.throwable.printStackTrace(PrintWriter(sw))
                    val errorMsg = "Network error sending message:$sw"
                    addLog(errorMsg)
                    addMessage(Message(errorMsg, MessageType.ERROR))
                }
                null -> addLog("Error: Session is not initialized.")
            }
        }
    }

    private fun addMessage(message: Message) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(message)
        _messages.value = currentMessages
    }
}