package com.example.ar_lab4

import android.graphics.BitmapFactory
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView

@Composable
fun ARScreen() {

    val context = LocalContext.current

    var detectedImages by remember {
        mutableStateOf<List<AugmentedImage>>(emptyList())
    }

    var modelRotation by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            planeRenderer = false,
            sessionConfiguration = { session, config ->
                val bitmap = BitmapFactory.decodeStream(
                    context.assets.open("1_number.jpg")
                )

                config.augmentedImageDatabase =
                    AugmentedImageDatabase(session).apply {
                        addImage(
                            "1_number",
                            bitmap,
                            0.15f
                        )
                    }
            },
            onSessionUpdated = { _, frame ->
                val images = frame
                    .getUpdatedTrackables(AugmentedImage::class.java)
                    .filter { it.trackingState == TrackingState.TRACKING }
                    .toList()

                if (images != detectedImages) detectedImages = images
            },
            onTouchEvent = { event, _ ->
                if (event.actionMasked == MotionEvent.ACTION_UP && detectedImages.isNotEmpty()) {
                    modelRotation += 45f
                }
                true
            }

        ) {

            detectedImages.forEach { augmentedImage ->
                val modelInstance = remember(augmentedImage.index) {
                    modelLoader.createModelInstance(
                        assetFileLocation = "squirrel.glb"
                    )
                }

                AugmentedImageNode(
                    augmentedImage = augmentedImage,
                    applyImageScale = true
                ) {
                    ModelNode(
                        modelInstance = modelInstance,
                        scaleToUnits = 0.2f,
                        rotation = io.github.sceneview.math.Rotation(y = modelRotation)
                    )
                }
            }
        }

        Text(
            text = if (detectedImages.isEmpty()) {
                "Наведите камеру на метку"
            } else {
                "Метка распознана! Нажмите на экран"
            },
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}