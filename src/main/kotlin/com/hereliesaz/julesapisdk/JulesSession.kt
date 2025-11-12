package com.hereliesaz.julesapisdk

class JulesSession(
    private val client: JulesClient,
    val session: Session
) {
    suspend fun sendMessage(prompt: String): SdkResult<MessageResponse> {
        return client.sendMessage(session.name, prompt)
    }

    suspend fun approvePlan(): SdkResult<Unit> {
        return client.approvePlan(session.name)
    }

    suspend fun listActivities(pageSize: Int? = null, pageToken: String? = null): SdkResult<ListActivitiesResponse> {
        return client.listActivities(session.name, pageSize, pageToken)
    }
}
