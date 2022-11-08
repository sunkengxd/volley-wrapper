package com.sunkengod.volleywrapper

import android.app.Application
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HttpHeaderParser
import com.sunkengod.volleywrapper.Volley.perform

private typealias MResponse = Response

private const val TAG = "Volley Request"

/**
 * Object for making network requests.
 *
 * Usage:
 * ```
 *      // provide application instance
 *      Volley {
 *          this.application = application
 *          // other parameters
 *      }
 *
 *      // anywhere in your app
 *      Volley perform Request(url)
 *          .onSuccess { doSomething() }
 *          .onFailure { doSomethingElse() }
 * ```
 *
 * @see Parameters
 */
object Volley {

    /**
     * Object containing various volley parameters.
     *
     * @see baseUrl
     * @see perform
     * @see log
     * @see timeoutMsMultiplier
     */
    object Parameters {

        /**
         * Application instance to provide via invoking Volley.
         *
         * @see Volley
         */
        lateinit var app: Application

        internal val appInitialized get() = ::app.isInitialized
        /**
         * Sets whether requests are logged or not.
         * If true, logs whenever a [Request] is added to the queue and if an error occurs.
         */
        var log = false

        /**
         * Milliseconds multiplier for [DefaultRetryPolicy.DEFAULT_TIMEOUT_MS]. Used to set a socket timeout per retry attempt.
         *
         * @see additionalRetries
         * @see backoffMultiplier
         */
        var timeoutMsMultiplier = 5

        /**
         * Gets added to [DefaultRetryPolicy.DEFAULT_MAX_RETRIES]. Number of times a retry is attempted.
         *
         * @see timeoutMsMultiplier
         * @see backoffMultiplier
         */
        var additionalRetries = 1

        /**
         * Is used to calculate socket timeout as listed below:
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
         * A URL appended to all requests.
         *
         * Example usage:
         * ```
         * homeDomain = "www.example.com"
         * volley perform Request("/example")
         * ```
         * will send GET to www.example.com/example
         */
        var baseUrl = ""
            set(value) {
                field = value.removeSuffix("/")
            }
    }

    operator fun invoke(block: Parameters.() -> Unit) = block(Parameters)

    private val queue: RequestQueue by lazy {
        com.android.volley.toolbox.Volley.newRequestQueue(Parameters.app)
    }

    /**
     * Perform network requests
     *
     * @param request [Request] object.
     */
    infix fun perform(request: Request) {
        if(!Parameters.appInitialized) throw UninitializedPropertyAccessException("Volley is not provided an app instance. Invoke `Volley { application = yourApplication }` or see usage.")
        val volleyRequest = object : com.android.volley.Request<Response>(request.method.key,
                                                                          request.url,
                                                                          com.android.volley.Response.ErrorListener {
                                                                              val code = it.networkResponse.statusCode
                                                                              val data =
                                                                                  it.networkResponse.data.toString()
                                                                              if(Parameters.log) Log.e(
                                                                                  TAG,
                                                                                  "URL: ${request.url}\n" + "Status: $code\n" + "Data: $data\n" + "Message: ${it.message}"
                                                                              )
                                                                              request.onFailure?.invoke(
                                                                                  code, data
                                                                              )
                                                                          }) {
            override fun getHeaders() = HashMap(request.headers)

            override fun getUrl() = if(Parameters.baseUrl.isEmpty()) request.url
            else Parameters.baseUrl + if(request.url.startsWith("/")) request.url else "/${request.url}"

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
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * Parameters.timeoutMsMultiplier,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES + Parameters.additionalRetries,
            Parameters.backoffMultiplier
        )

        queue.add(volleyRequest).also {
            if(Parameters.log) Log.d(
                TAG, "URL: ${it.url}\n" + "HEAD: ${it.headers}\n" + "BODY: ${String(it.body)}"
            )
        }
    }
}

