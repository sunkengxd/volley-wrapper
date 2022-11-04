# Volley Wrapper
## A wrapper around Google's Volley.
---

This project's goal is to make using Volley as simple as calling one function.

### Example usage:

```kotlin
val volley = Volley.initialize(context)

// infix
volley perform Request("https://catfact.ninja")

// or dot call
volley.perform(Request("https://catfact.ninja"))
```

You can set `onSuccess()` and `onFailure()` callback on the `Request` object to handle API responses.

### Complex request

Perform a POST request

```kotlin
// set default domain
Volley.homeDomain = "www.example.com"

// supports infix notation as well
val volley = Volley initialize context

// url '/' prefix is not mandatory
volley perform Request("users",
    method = RequestMethod.POST
    headers = mapOf("Content-Type" to "application/json"),
    body = JSONObject().apply {
        put("first_name", "john")
        put("last_name", "doe")
    }).onSuccess { doSomething() }
    .onFailure { doSomethingElse() }
```
---

Perform a GET with query options

```kotlin
// set default domain
Volley.homeDomain = "www.example.com"

val volley = Volley initialize context

volley perform Request("/countries",
    queryOptions = mapOf("continent" to "eurasia")
    .onSuccess { doSomething() }
    .onFailure { doSomethingElse() }
```