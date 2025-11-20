package com.example.mappastellareapk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.mappastellareapk.ui.theme.MappaStellareAPKTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.drawscope.withTransform


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MappaStellareAPKTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }

            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val stars = StarFieldFromCatalog()
    StarsFromCatalog(stars = stars)
}


@Composable
fun StarFieldFromCatalog(): List<Offset> {
    val context = LocalContext.current

    return remember {
        val lines = context.assets.open("catalog")
            .bufferedReader()
            .readLines()

        val list = mutableListOf<Offset>()

        var x = 10f
        var y = 10f

        for ((index, _) in lines.withIndex()) {
            list.add(Offset(x, y))

            x += 10f
            if ((index + 1) % 10 == 0) {
                x = 10f
                y += 10f
            }
        }

        list
    }
}


@Composable
fun StarsFromCatalog(stars: List<Offset>) {

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale
                    val newScale = (oldScale * zoom).coerceIn(0.1f, 12f)

                    // fattore di cambiamento della scala
                    val scaleChange = newScale / oldScale

                    // offset è una traslazione in coordinate schermo
                    // mapping: screen = world * scale + offset
                    // aggiorniamo offset per zoomare attorno al centroid
                    offset = (offset - centroid) * scaleChange + centroid + pan

                    scale = newScale
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            withTransform({
                // PRIMA scala, POI trasla: offset è in pixel schermo
                scale(scale, scale)
                translate(offset.x, offset.y)
            }) {
                stars.forEach { star ->
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = star
                    )
                }
            }
        }
    }
}
