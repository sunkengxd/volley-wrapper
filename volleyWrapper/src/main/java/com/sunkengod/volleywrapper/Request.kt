package com.sunkengod.volleywrapper

import com.android.volley.VolleyError
import org.json.JSONObject

/**
 * Class containing a positive response code and a [ByteArray] body.
 */
class Response internal constructor(
    val status: Int, private val response: ByteArray
) {

    /**
     * Get response body as a [ByteArray].
     */
    val bytes get() = response

    /**
     * Get response body as a [String].
     */
    val string by lazy { String(response) }

    /**
     * Get response body as a [JSONObject].
     */
    val json by lazy { kotlin.runCatching { JSONObject(string) }.getOrNull() }

    /**
     * Get response body as a formatted JSON [String].
     */
    val pretty: String? by lazy { json?.toString(4) }
}

/**
 * Class for building network requests using [Volley].
 *
 * @param destination URL where to send the request. Set a [Volley.homeDomain] or supply a full URL.
 * @param method [RequestMethod] to be performed.
 * @param headers Request head contents.
 * @param body Request body contents.
 * @param queryOptions Map which will be formatted and appended to URL.
 */
data class Request(
    private val destination: String,
    val method: RequestMethod = RequestMethod.GET,
    val headers: Map<String, String> = mapOf("Content-Type" to "application/json"),
    val body: JSONObject? = null,
    private val queryOptions: Map<String, String>? = null
) {

    val url = destination + if(!queryOptions.isNullOrEmpty()) buildString {
        append("?")
        queryOptions.forEach { (k, v) ->
            append("$k=$v&")
        }
        deleteCharAt(length - 1)
    } else ""

    private var _onSuccess: ((Response) -> Unit)? = null
    private var _onFailure: ((Int, String) -> Unit)? = null

    /**
     * Reference to the optional success lambda.
     */
    val onSuccess get() = _onSuccess

    /**
     * Reference to the optional failure lambda.
     */
    val onFailure get() = _onFailure

    /**
     * Set a onSuccess callback for this request.
     *
     * @param lambda The callback, receives [Response].
     * @return Copy of the request with said callback.
     */
    fun onSuccess(lambda: (Response) -> Unit) = this.copy().apply { _onSuccess = lambda }

    /**
     * Set a onFailure callback for this request.
     *
     * @param lambda The callback, receives [Int] status code and [VolleyError].
     * @return Copy of the request with said callback.
     */
    fun onFailure(lambda: (Int, String) -> Unit) = this.copy().apply { _onFailure = lambda }
}