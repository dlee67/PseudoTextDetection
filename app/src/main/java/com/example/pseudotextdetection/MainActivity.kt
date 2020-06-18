package com.example.pseudotextdetection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.ArFragment

class MainActivity : AppCompatActivity() {

    // Map is the most pragmatic data structure for persisting images to track, taking the
    // consideration that ARCore can track 20 images at the same time.
    private val augmentedImageMap: MutableMap<AugmentedImage, AugmentedImageNode> = mutableMapOf()
    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.arFragment = supportFragmentManager.findFragmentById(R.id.arFrag) as ArFragment
        // https://developers.google.com/sceneform/reference/com/google/ar/sceneform/Scene#addOnUpdateListener(com.google.ar.sceneform.Scene.OnUpdateListener)
        // Literally, every Frame.
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
        // https://developers.google.com/sceneform/reference/com/google/ar/sceneform/Scene?hl=in
        // Scene is what I typically refer as the "AR space."
    }

    private fun onUpdateFrame(frameTime: FrameTime) {
        Log.i("dhl", "\n\n\n\nIn onUpdateFrame of MainActivity.");
        val frame = arFragment.arSceneView.arFrame

        frame?.let { // This is something specific to Kotlin.
            // https://developers.google.com/ar/reference/c/group/augmented-image
            // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Frame#getUpdatedTrackables(java.lang.Class%3CT%3E)
            val updatedAugmentedImages = it.getUpdatedTrackables(AugmentedImage::class.java) // Whatever trackable that is an augmented image ...
            updatedAugmentedImages.forEach { augmentedImage ->
                when (augmentedImage.trackingState) {
                    // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/TrackingState
                    TrackingState.PAUSED -> {
                        val message = "Detected image " + augmentedImage.name
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                    TrackingState.TRACKING -> { // When finding the image that needs to be tracked.
                        if (!augmentedImageMap.containsKey(augmentedImage)) {
                            val node = AugmentedImageNode(this, arFragment)
                            node.setImageToNode(augmentedImage) // Attaches the renderable here.
                            augmentedImageMap.put(augmentedImage, node)
                            arFragment.arSceneView.scene.addChild(node)
                        }
                    }
                    TrackingState.STOPPED -> {
                        augmentedImageMap.remove(augmentedImage)
                    }
                    else -> {
                        Log.d("dhl", "Unknown image state")
                    }
                }
            }
        }
    }
}