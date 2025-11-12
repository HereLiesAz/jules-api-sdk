package com.jules.sdk

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JulesClientTest {

    private lateinit var mockHttpClient: JulesHttpClient
    private lateinit var julesClient: JulesClient

    @BeforeEach
    fun setUp() {
        mockHttpClient = mockk(relaxed = true)
        julesClient = JulesClient(mockHttpClient)
    }

    @Test
    fun `listSources calls http client get`() = runBlocking {
        julesClient.listSources()
        coVerify { mockHttpClient.get<ListSourcesResponse>("/sources", any()) }
    }

    @Test
    fun `getSource calls http client get`() = runBlocking {
        julesClient.getSource("test-id")
        coVerify { mockHttpClient.get<Source>("/sources/test-id") }
    }

    @Test
    fun `createSession calls http client post`() = runBlocking {
        val request = mockk<CreateSessionRequest>()
        julesClient.createSession(request)
        coVerify { mockHttpClient.post<Session>("/sessions", request) }
    }

    @Test
    fun `listSessions calls http client get`() = runBlocking {
        julesClient.listSessions()
        coVerify { mockHttpClient.get<ListSessionsResponse>("/sessions", any()) }
    }

    @Test
    fun `getSession calls http client get`() = runBlocking {
        julesClient.getSession("test-id")
        coVerify { mockHttpClient.get<Session>("/sessions/test-id") }
    }

    @Test
    fun `approvePlan calls http client post`() = runBlocking {
        julesClient.approvePlan("test-id")
        coVerify { mockHttpClient.post<Unit>("/sessions/test-id:approvePlan") }
    }

    @Test
    fun `listActivities calls http client get`() = runBlocking {
        julesClient.listActivities("test-id")
        coVerify { mockHttpClient.get<ListActivitiesResponse>("/sessions/test-id/activities", any()) }
    }

    @Test
    fun `getActivity calls http client get`() = runBlocking {
        julesClient.getActivity("session-id", "activity-id")
        coVerify { mockHttpClient.get<Activity>("/sessions/session-id/activities/activity-id") }
    }

    @Test
    fun `sendMessage calls http client post`() = runBlocking {
        julesClient.sendMessage("test-id", "prompt")
        coVerify { mockHttpClient.post<MessageResponse>("/sessions/test-id:sendMessage", any()) }
    }
}
