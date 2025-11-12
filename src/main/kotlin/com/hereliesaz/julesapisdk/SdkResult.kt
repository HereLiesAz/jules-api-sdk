package com.hereliesaz.julesapisdk

/**
 * A sealed class representing the result of an SDK operation.
 * It can be either a [Success], an [Error], or a [NetworkError].
 *
 * @param T The type of the successful data.
 */
sealed class SdkResult<out T> {
    /**
     * Represents a successful operation.
     * @property data The data returned by the operation.
     */
    data class Success<T>(val data: T) : SdkResult<T>()

    /**
     * Represents a known API error (e.g., 4xx or 5xx response).
     * @property code The HTTP status code of the error.
     * @property body The raw response body of the error.
     */
    data class Error(val code: Int, val body: String) : SdkResult<Nothing>()

    /**
     * Represents a network or unexpected exception during the operation.
     * @property throwable The underlying [Throwable] that occurred.
     */
    data class NetworkError(val throwable: Throwable) : SdkResult<Nothing>()
}
