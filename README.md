# Jules AI Kotlin SDK

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> **Note:** This is an **unofficial** community SDK for the Jules AI API. It is not affiliated with, officially maintained by, or endorsed by Google.

A fully-typed Kotlin SDK for the [Jules AI API](https://developers.google.com/jules/api), built with Ktor and Kotlinx Serialization. [Jules](https://jules.google.com) is a Google product that provides AI-powered coding assistance.

This SDK provides a simple, asynchronous, and type-safe way to interact with all Jules API endpoints from any Kotlin application.

**SDK Version:** 1.0.1 (implementing Jules API v1alpha, last updated 2025-11-10 UTC)

## Installation

Add the following dependency to your `build.gradle.kts` or `build.gradle` file:

**Gradle (Kotlin DSL)**
```kotlin
implementation("com.hereliesaz.julesapisdk:kotlin-sdk:1.0.2")
```

## Quick Start for Android

All SDK methods are `suspend` functions and are main-safe. They should be called from a CoroutineScope, such as the `viewModelScope` in an Android ViewModel.

### 1. Get the JulesClient
```kotlin
// In your Hilt/Koin module or application class
val client = JulesClient(apiKey = "YOUR_API_KEY")
```

### 2. Call the SDK from your ViewModel
```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.julesapisdk.JulesClient
import com.hereliesaz.julesapisdk.CreateSessionRequest
import com.hereliesaz.julesapisdk.SourceContext
import kotlinx.coroutines.launch

class MyViewModel(private val julesClient: JulesClient) : ViewModel() {

    fun createMySession() {
        viewModelScope.launch { // Always use a scope like viewModelScope
            val sessionRequest = CreateSessionRequest(
                prompt = "Create a boba app!",
                sourceContext = SourceContext(source = "my-source"),
                title = "Boba App"
            )

            when (val result = julesClient.createSession(sessionRequest)) {
                is SdkResult.Success -> {
                    val session = result.data
                    println("Created session: ${session.id}")
                    // Update your UI state with the session
                }
                is SdkResult.Error -> {
                    println("API Error: ${result.code} - ${result.body}")
                    // Update your UI state with the API error
                }
                is SdkResult.NetworkError -> {
                    println("Network Error: ${result.throwable.message}")
                    // Update your UI state with the network error
                }
            }
        }
    }
}
```

## Android Test App

The included Android test app in the `android-test-app` directory provides a simple way to test the SDK's functionality. To use the test app, you need to provide a valid API key and a source.

### Configuring the Test App

1.  **API Key**: Enter your Jules API key in the "API Key" field in the app's settings.
2.  **Source**: Enter a valid source in the "Source" field. A valid source is a string in the format `sources/github/<owner>/<repo>`. For example, `sources/github/google/jules`. You can find a list of your available sources by using the `listSources()` method in the SDK or by checking the Jules web app.

## API Methods

All methods are `suspend` functions and should be called from a coroutine.

- **Sources**: `listSources()`, `getSource(sourceId)`
- **Sessions**: `createSession(request)`, `listSessions()`, `getSession(sessionId)`, `approvePlan(sessionId)`
- **Activities**: `listActivities(sessionId)`, `getActivity(sessionId, activityId)`
- **Messages**: `sendMessage(sessionId, prompt)`

## API Reference

### JulesClient

#### Constructor

```kotlin
val client = JulesClient(
    apiKey: String,
    baseUrl: String = "https://jules.googleapis.com",
    apiVersion: String = "v1alpha",
    retryConfig: RetryConfig = RetryConfig()
)
```

**Parameters:**
- `apiKey`: Your Jules API key (required).
- `baseUrl`: The base URL for the Jules API (optional, defaults to `https://jules.googleapis.com`).
- `apiVersion`: The version of the Jules API to use (optional, defaults to `v1alpha`).
- `retryConfig`: Configuration for request retries (optional).
  - `maxRetries`: Maximum number of retry attempts (default: 0, retries are disabled by default).
  - `initialDelayMs`: The initial delay in milliseconds before the first retry (default: 1000).

**Example with all options:**
```kotlin
val client = JulesClient(
    apiKey = System.getenv("JULES_API_KEY")!!,
    baseUrl = "https://jules.googleapis.com",
    apiVersion = "v1alpha",
    retryConfig = RetryConfig(
        maxRetries = 5,
        initialDelayMs = 500
    )
)
```

#### Methods

All methods are `suspend` functions.

##### `listSources(pageSize: Int?, pageToken: String?, filter: String?): ListSourcesResponse`
List all available sources.

##### `getSource(sourceId: String): Source`
Get details of a specific source.

##### `createSession(request: CreateSessionRequest): Session`
Create a new session.

##### `listSessions(pageSize: Int?, pageToken: String?): ListSessionsResponse`
List all sessions.

##### `getSession(sessionId: String): Session`
Get details of a specific session.

##### `approvePlan(sessionId: String)`
Approve the latest plan for a session.

##### `listActivities(sessionId: String, pageSize: Int?, pageToken: String?): ListActivitiesResponse`
List activities for a session.

##### `getActivity(sessionId: String, activityId: String): Activity`
Get a specific activity for a session.

##### `sendMessage(sessionId: String, prompt: String)`
Send a message to the agent in a session.

## Authentication & Security

All API requests require authentication using your Jules API key. Get your API key from the Settings page in the [Jules web app](https://jules.google.com). The client automatically includes the key in the `X-Goog-Api-Key` header for all requests.

**Important:** Keep your API keys secure. Never commit them to version control or expose them in client-side code. For Android applications, we strongly recommend storing your API key securely using a mechanism like Android's [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences). The included reference test application demonstrates this best practice.

## Error Handling & Resilience

The SDK is built on Ktor and provides robust error handling features.

### Automatic Retries

Retries are **disabled by default**. To enable them, provide a `RetryConfig` with `maxRetries` greater than 0.

```kotlin
val client = JulesClient(
    apiKey = System.getenv("JULES_API_KEY")!!,
    retryConfig = RetryConfig(
        maxRetries = 5,
        initialDelayMs = 500
    )
)
```


## Resources

- [Official Jules API Documentation](https://developers.google.com/jules/api)
- [Jules Web App](https://jules.google.com)
- [GitHub Repository](https://github.com/hereliesaz/jules-api-sdk)


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
