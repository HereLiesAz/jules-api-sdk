package com.jules.sdk

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JulesClientTest {

    private fun createMockClient(mockResponses: Map<String, String>): JulesClient {
        val mockEngine = MockEngine { request ->
            val responseContent = mockResponses[request.url.encodedPath] ?: ""
            respond(
                content = responseContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
        }
        return JulesClient(JulesHttpClient(apiKey = "test-key", httpClient = httpClient))
    }

    private fun readResource(name: String): String {
        return this::class.java.getResource(name)!!.readText()
    }

    @Test
    fun `listSources returns sources`() = runBlocking {
        val mockResponse = readResource("/listSources.json")
        val client = createMockClient(mapOf("/sources" to mockResponse))
        val response = client.listSources()
        val expected = Json.decodeFromString<ListSourcesResponse>(mockResponse)
        assertEquals(expected, response)
    }

    @Test
    fun `getSource returns source`() = runBlocking {
        val mockResponse = readResource("/getSource.json")
        val client = createMockClient(mapOf("/sources/test-id" to mockResponse))
        val response = client.getSource("test-id")
        val expected = Json.decodeFromString<Source>(mockResponse)
        assertEquals(expected, response)
    }

    @Test
    fun `createSession returns session`() = runBlocking {
        val mockResponse = readResource("/createSession.json")
        val client = createMockClient(mapOf("/sessions" to mockResponse))
        val response = client.createSession(CreateSessionRequest("prompt", SourceContext("source")))
        val expected = Json.decodeFromString<Session>(mockResponse)
        assertEquals(expected, response)
    }

    @Test
    fun `listSessions returns sessions`() = runBlocking {
        val mockResponse = readResource("/listSessions.json")
        val client = createMockClient(mapOf("/sessions" to mockResponse))
        val response = client.listSessions()
        val expected = Json.decodeFromString<ListSessionsResponse>(mockResponse)
        assertEquals(expected, response)
    }

    @Test
    fun `getSession returns session`() = runBlocking {
        val mockResponse = readResource("/getSession.json")
        val client = createMockClient(mapOf("/sessions/test-id" to mockResponse))
        val response = client.getSession("test-id")
        val expected = Json.decodeFromString<Session>(mockResponse)
        assertEquals(expected, response)
    }

    @Test
    fun `approvePlan works`() = runBlocking {
        val client = createMockClient(mapOf("/sessions/test-id:approvePlan" to "{}"))
        client.approvePlan("test-id")
    }

    @Test
    fun `listActivities returns activities`() = runBlocking {
        val mockResponse = readResource("/listActivities.json")
        val client = createMockClient(mapOf("/sessions/test-id/activities" to mockResponse))
        val response = client.listActivities("test-id")
        val expected = Json.decodeFromString<ListActivitiesResponse>(mockResponse)
        assertEquals(expected, response)
    }

    @Test
    fun `getActivity returns activity`() = runBlocking {
        val mockResponse = readResource("/getActivity.json")
        val client = createMockClient(mapOf("/sessions/session-id/activities/activity-id" to mockResponse))
        val response = client.getActivity("session-id", "activity-id")
        val expected = Json.decodeFromString<Activity>(mockResponse)
        assertEquals(expected, response)
    }

    @Test
    fun `sendMessage returns message`() = runBlocking {
        val mockResponse = readResource("/sendMessage.json")
        val client = createMockClient(mapOf("/sessions/test-id:sendMessage" to mockResponse))
        val response = client.sendMessage("test-id", "prompt")
        val expected = Json.decodeFromString<MessageResponse>(mockResponse)
        assertEquals(expected, response)
    }
}
