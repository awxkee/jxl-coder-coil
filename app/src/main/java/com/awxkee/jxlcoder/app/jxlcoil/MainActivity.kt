package com.awxkee.jxlcoder.app.jxlcoil

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.bitmapConfig
import com.awxkee.jxlcoder.app.jxlcoil.ui.theme.JxlCoilTheme
import com.awxkee.jxlcoder.coil.AnimatedJxlDecoder
import com.awxkee.jxlcoder.coil.JxlDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val imageLoader = ImageLoader.Builder(this)
                .components {
                    add(AnimatedJxlDecoder.Factory())
                }
//                .allowHardware(true)
//                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build()
            JxlCoilTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        AsyncImage(
                            model = ImageRequest.Builder(context = LocalContext.current)
                                .data("https://github.com/libjxl/conformance/raw/refs/heads/master/testcases/cafe/input.jxl")
                                .build(),
                            contentDescription = null,
                            imageLoader = imageLoader,
                            modifier = Modifier.width(250.dp).height(250.dp),
                            contentScale = ContentScale.Fit,
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context = LocalContext.current)
                                .data("https://github.com/libjxl/conformance/raw/refs/heads/master/testcases/animation_icos4d/input.jxl")
                                .build(),
                            contentDescription = null,
                            imageLoader = imageLoader,
                            modifier = Modifier.width(250.dp).height(250.dp),
                            contentScale = ContentScale.Fit,
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context = LocalContext.current)
                                .data("https://www.earth.org.uk/img/boiler/boiler-portrait-posterised-interlaced-256w.png.jxl")
                                .build(),
                            contentDescription = null,
                            imageLoader = imageLoader,
                            modifier = Modifier.width(250.dp).height(250.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JxlCoilTheme {
        Greeting("Android")
    }
}