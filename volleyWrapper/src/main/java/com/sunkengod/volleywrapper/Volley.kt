package com.sunkengod.volleywrapper

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HttpHeaderParser
import com.sunkengod.volleywrapper.Volley.Companion.getInstance
import com.sunkengod.volleywrapper.Volley.Companion.homeDomain
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope

private typealias MResponse = Response

/**
 * Class for making network requests.
 *
 * @see getInstance
 * @see homeDomain
 * @see perform
 */
class Volley private constructor(context: Context) {

    companion object {

        var log = false

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
        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
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
        val volleyRequest = object : com.android.volley.Request<Response>(request.method.key,
                                                                          request.url,
                                                                          com.android.volley.Response.ErrorListener {
                                                                              val code = it.networkResponse.statusCode
                                                                              val data = String(it.networkResponse.data)
                                                                              if(log) Log.e("VolleyRequest",
                                                                              "URL: ${request.url}\n" +
                                                                                      "Status: $code\n" +
                                                                                      "Data: $data\n" +
                                                                                      "Message: ${it.message}")
                                                                              request.onFailure?.invoke(
                                                                                  code,
                                                                                  it
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
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES + 1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(volleyRequest).also {
            if(log) Log.d(
                "VolleyRequest",
                "URL: ${it.url}\n" + "HEAD: ${it.headers}\n" + "BODY: ${String(it.body)}"
            )
        }
    }
}

