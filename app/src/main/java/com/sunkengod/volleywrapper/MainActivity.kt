package com.sunkengod.volleywrapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sunkengod.volleywrapper.ui.theme.VolleyWrapperTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Volley.homeDomain = "https://catfact.ninja"
        Volley.log = true
        setContent {
            VolleyWrapperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val volley = Volley initialize this

                    val r = remember { mutableStateOf<Response?>(null) }
                    Button(modifier = Modifier
                        .wrapContentSize()
                        .padding(30.dp)
                        .animateContentSize(),
                           onClick = {
                               volley perform Request("fact").onSuccess { r.value = it }
                           }) {
                        AnimatedContent(targetState = r) {
                            Text(
                                text = r.value?.json?.optString("fact", "Nope")
                                    ?: "Request cat fact",
                                modifier = Modifier.padding(horizontal = 0.dp, vertical = 15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VolleyWrapperTheme {
    }
}