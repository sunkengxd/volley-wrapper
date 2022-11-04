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
    val json by lazy { JSONObject(string) }

    /**
     * Get response body as a formatted JSON [String].
     */
    val pretty: String by lazy { JSONObject(string).toString(4) }
}

/**
 * Class for building network requests using [Volley].
 *
 * @see destination
 * @see method
 * @see headers
 * @see body
 * @see queryOptions
 *
 * @property destination URL where to send the request. Set a [Volley.homeDomain] or supply a full URL.
 * @property method [RequestMethod] to be performed.
 * @property headers HEAD contents.
 * @property body BODY contents
 * @property queryOptions Map which will be formatted and appended to URL.
 */
data class Request(
    private val destination: String,
    val method: RequestMethod = RequestMethod.GET,
    val headers: Map<String, String> = mapOf("Content-Type" to JSON),
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
    private var _onFailure: ((Int, VolleyError) -> Unit)? = null

    val onSuccess get() = _onSuccess
    val onFailure get() = _onFailure

    fun onSuccess(lambda: (Response) -> Unit) = this.copy().apply { _onSuccess = lambda }
    fun onFailure(lambda: (Int, VolleyError) -> Unit) = this.copy().apply { _onFailure = lambda }
}