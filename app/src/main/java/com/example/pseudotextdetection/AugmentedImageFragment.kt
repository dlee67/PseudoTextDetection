package com.example.pseudotextdetection

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException

// It's easier to think of ArFragment as the scene itself.
class AugmentedImageFragment : ArFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        // https://developers.google.com/sceneform/reference/com/google/ar/sceneform/ux/PlaneDiscoveryController
        // Kotlin can grab inheriting objects like this.
        planeDiscoveryController.apply {
            hide()
            setInstructionView(null)
        }
        // https://developers.google.com/sceneform/reference/com/google/ar/sceneform/rendering/PlaneRenderer
        // // Kotlin can grab inheriting objects like this.
        arSceneView.planeRenderer.isEnabled = false
        return view
    }

    // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session
    override fun getSessionConfiguration(session: Session?): Config {
        Log.i("dhl", "Within the getSessionConfiguration.");
        val config = super.getSessionConfiguration(session)
        if (!setupImageDatabase(config, session)) {
            Log.e("dhl", "Could not set up augmented image database")
            Toast.makeText(requireContext(), "Could not set up augmented image database", Toast.LENGTH_LONG).show()
        }
        // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Config
        config.focusMode =
            Config.FocusMode.AUTO // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Config#getFocusMode()
        config.updateMode =
            Config.UpdateMode.LATEST_CAMERA_IMAGE // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Config.UpdateMode
        return config
    }

    private fun setupImageDatabase(config: Config, session: Session?): Boolean {
        Log.i("dhl", "In the setUpImageDatabase.");
        val imageDatabase = AugmentedImageDatabase(session)
        return try {
            requireContext().assets.open("tofu_soup.png").use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageDatabase.addImage("tofu_soup", bitmap, 0.12f)
            }
            requireContext().assets.open("cpp-alliance.png").use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageDatabase.addImage("cpp-alliance", bitmap, 0.12f)
            }
            requireContext().assets.open("huge-cardboard.png").use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageDatabase.addImage("huge-cardboard", bitmap, 0.12f)
            }
            config.augmentedImageDatabase = imageDatabase
            true
        } catch (e: IOException) {
            Log.e("dhl", "IO exception loading augmented image database.", e)
            false
        }
    }
}
