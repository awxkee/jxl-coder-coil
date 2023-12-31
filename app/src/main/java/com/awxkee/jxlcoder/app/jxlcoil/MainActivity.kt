package com.awxkee.jxlcoder.app.jxlcoil

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import coil.compose.AsyncImage
import com.awxkee.jxlcoder.app.jxlcoil.ui.theme.JxlCoilTheme
import com.awxkee.jxlcoder.coil.JxlDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val imageLoader = ImageLoader.Builder(this)
                .components {
                    add(JxlDecoder.Factory())
                }
                .allowHardware(false)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build()
            JxlCoilTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        AsyncImage(
                            model = "https://backup-csh.fra1.digitaloceanspaces.com/pexels-emre-ug%CC%86urlar-19716967.jxl",
                            contentDescription = null,
                            imageLoader = imageLoader,
                        )
                        AsyncImage(
                            model = "https://backup-csh.fra1.digitaloceanspaces.com/pexels-thibaut-tattevin-18273081.jxl",
                            contentDescription = null,
                            imageLoader = imageLoader,
                        )
                        AsyncImage(
                            model = "https://backup-csh.fra1.digitaloceanspaces.com/dark_street.jxl",
                            contentDescription = null,
                            imageLoader = imageLoader,
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