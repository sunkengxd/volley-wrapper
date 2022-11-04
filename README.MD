# Volley Wrapper
## A wrapper around Google's Volley.
---

This project's goal is to make using Volley as simple as calling one function.

Example usage:

```kotlin
val volley = Volley.getInstance(context)

volley perform Request("https://catfact.ninja")
```

You can set `onSuccess()` and `onFailure()` callback on the `Request` object to handle API responses.