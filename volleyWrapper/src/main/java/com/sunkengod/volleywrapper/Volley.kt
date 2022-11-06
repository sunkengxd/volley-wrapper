package com.sunkengod.volleywrapper

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HttpHeaderParser
import com.sunkengod.volleywrapper.Volley.Companion.initialize
import com.sunkengod.volleywrapper.Volley.Companion.homeDomain

private typealias MResponse = Response

private const val TAG = "Volley Request"

/**
 * Class for making network requests.
 *
 * Usage:
 * ```
 *      // somewhere in your code
 *      val volley = Volley initialize context
 *      volley perform Request(url)
 *          .onSuccess { doSomething() }
 *          .onFailure { doSomethingElse() }
 * ```
 * @see initialize
 * @see homeDomain
 * @see perform
 * @see log
 * @see timeoutMsMultiplier
 */
class Volley private constructor(context: Context) {

    companion object {

        /**
         * Sets whether requests are logged or not.
         * If true, logs whenever a [Request] is added to the queue and if an error occurs.
         */
        var log = false

        /**
         * Timeout milliseconds multiplier for [DefaultRetryPolicy.DEFAULT_TIMEOUT_MS]. Used to set a socket timeout per retry attempt.
         *
         * @see additionalRetries
         * @see backoffMultiplier
         */
        var timeoutMsMultiplier = 5

        /**
         * Additional retries. Added to [DefaultRetryPolicy.DEFAULT_MAX_RETRIES]. Number of times a retry is attempted.
         *
         * @see timeoutMsMultiplier
         * @see backoffMultiplier
         */
        var additionalRetries = 1

        /**
         * Backoff multiplier. Timeout multiplier.
         *```
         * timeout = 250 * timeoutMsMultiplier
         * socketTimeout = timeout + timeout * backoffMultiplier
         * ```
         *
         * @see timeoutMsMultiplier
         * @see additionalRetries
         */
        var backoffMultiplier = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT

        /**
         * A URL appended to all requests
         *
         * Example usage:
         * ```
         * homeDomain = "www.example.com"
         * volley perform Request("/example")
         * ```
         * will send GET to www.example.com/example
         */
        var homeDomain = ""
            set(value) {
                field = value.removeSuffix("/")
            }

        @Volatile
        private var INSTANCE: Volley? = null

        /**
         * Use this to initialize the Volley singleton with a context.
         *
         * @param context A Context to use for creating the cache dir.
         */
        @JvmStatic
        infix fun initialize(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Volley(context).also {
                INSTANCE = it
            }
        }
    }

    private val queue: RequestQueue by lazy {
        com.android.volley.toolbox.Volley.newRequestQueue(context.applicationContext)
    }

    /**
     * Perform network requests
     *
     * @param request [Request] object.
     */
    infix fun perform(request: Request) {
        val volleyRequest = object : com.android.volley.Request<Response>(
            request.method.key,
            request.url,
            com.android.volley.Response.ErrorListener {
                val code = it.networkResponse.statusCode
                val data = it.networkResponse.data
                if(log) Log.e(
                    TAG,
                    "URL: ${request.url}\n" + "Status: $code\n" + "Data: $data\n" + "Message: ${it.message}"
                )
                request.onFailure?.invoke(
                    code, it
                )
            }) {
            override fun getHeaders() = HashMap(request.headers)

            override fun getUrl() = if(homeDomain.isEmpty()) request.url
            else homeDomain + if(request.url.startsWith("/")) request.url else "/${request.url}"

            override fun getBody() = request.body.toString().toByteArray()

            override fun getBodyContentType() = request.headers["Content-Type"]

            override fun parseNetworkResponse(response: NetworkResponse) =
                com.android.volley.Response.success(
                    Response(
                        response.statusCode, response.data
                    ), HttpHeaderParser.parseCacheHeaders(response)
                )

            override fun deliverResponse(response: MResponse?) {
                if(response != null) {
                    request.onSuccess?.invoke(response)
                }
            }
        }
        volleyRequest.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * timeoutMsMultiplier,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES + additionalRetries,
            backoffMultiplier
        )

        queue.add(volleyRequest).also {
            if(log) Log.d(
                TAG, "URL: ${it.url}\n" + "HEAD: ${it.headers}\n" + "BODY: ${String(it.body)}"
            )
        }
    }
}

