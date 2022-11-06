# Volley Wrapper
## A wrapper around Google's Volley.

This project's goal is to make using Volley as simple as calling one function.

### Contents
1. [Installation](#installation)
2. [Usage](#example-usage)
3. [Properties](#properties)

### Installation
1. Add the following to your project's `settings.gradle`:
```gradle
dependencyResolutionManagement {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the following to your module's `build.gradle`:
```gradle
dependencies {
	        implementation 'com.github.sunkengxd:volley-wrapper:Tag'
	}
```

### Example usage:

```kotlin
val volley = Volley.initialize(context)

// infix
volley perform Request("https://catfact.ninja/fact")

// or dot call
volley.perform(Request("https://catfact.ninja/fact"))
```

You can set `onSuccess()` and `onFailure()` callbacks on the `Request` object to handle API responses.

#### Complex requests

- ##### Perform a POST request

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
    })
    .onSuccess { doSomething() }
    .onFailure { doSomethingElse() }
```

- ##### Perform a GET with query options

```kotlin
// set default domain
Volley.homeDomain = "www.example.com"

val volley = Volley initialize context

// GET www.example.com/countries?continent=eurasia
volley perform Request("/countries",
    queryOptions = mapOf("continent" to "eurasia")
    .onSuccess { doSomething() }
    .onFailure { doSomethingElse() }
```
---

### Properties
- `log` — Sets whether requests are logged or not. If true, logs whenever a [Request] is added to the queue and if an error occurs.
- `homeDomain` — A URL appended to all requests.
- `timeoutMsMultiplier` — Used to set a socket timeout per retry attempt.
- `additionalRetries` — Number of times a retry is attempted.
- `backoffMultiplier` — Used to calculate socket timeout as listed below:
```python
timeout = 250 * timeoutMsMultiplier
socketTimeout = timeout + timeout * backoffMultiplier
```