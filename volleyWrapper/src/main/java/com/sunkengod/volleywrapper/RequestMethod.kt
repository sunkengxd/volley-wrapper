package com.sunkengod.volleywrapper

import com.android.volley.Request

enum class RequestMethod(val key: Int) {
    DELETE(Request.Method.DELETE),
    GET(Request.Method.GET),
    HEAD(Request.Method.HEAD),
    OPTIONS(Request.Method.OPTIONS),
    PATCH(Request.Method.PATCH),
    POST(Request.Method.POST),
    PUT(Request.Method.PUT),
    TRACE(Request.Method.TRACE)
}