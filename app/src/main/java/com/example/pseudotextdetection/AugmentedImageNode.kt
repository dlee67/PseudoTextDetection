package com.example.pseudotextdetection

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.DpToMetersViewSizer
import com.google.ar.sceneform.rendering.ViewRenderable
import java.util.concurrent.CompletableFuture

// https://developers.google.com/sceneform/reference/com/google/ar/sceneform/AnchorNode
// This is what's allowing the ar objects to be trackable.
// To me, Node is something that encapsulates a renderable,
// which contains things like anchor (https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Anchor)
class AugmentedImageNode(context: Context) : AnchorNode() {

    private val gifViewLoader: CompletableFuture<ViewRenderable> = ViewRenderable
        .builder()
        .setView(context, R.layout.image_item)
        .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
        .build()
    private var gifView: ViewRenderable? = null
    private lateinit var image: AugmentedImage

    init {
        gifViewLoader.thenAccept { viewRenderable ->
            run {
                // https://developers.google.com/sceneform/develop/create-renderables
                gifView = viewRenderable // AR object that can be used in the scene.
            }
        }
    }

    fun setImageToNode(image: AugmentedImage) {
        Log.i("dhl", "\n\n\n\n\nIn setImageToNode.")
        this.image = image

        if (!gifViewLoader.isDone) {
            Log.i("dhl", "\n\n\n\n\nIn gifViewLoading.isDone.")
            gifViewLoader
                .thenAccept {
                    gifView = it
                    setImageToNode(image) // Recursively wait for the CompatibleFuture to complete ... shouldn't be there a sleep() at least?
                }
            gifViewLoader.exceptionally { throwable: Throwable ->
                Log.e("AugmentedImageNode", "failed to load", throwable)
                null
            }
            return
        }

        // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Trackable#createAnchor(com.google.ar.core.Pose)
        // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/AugmentedImage#getCenterPose()
        anchor = image.createAnchor(image.centerPose)

        // TODO Get actual width of image dynamically, could just be luck that GIPHY images are 400 pixels wide
        val imageWidth = 400
        val viewWidth = (imageWidth / image.extentX).toInt()
        gifView?.sizer = DpToMetersViewSizer(viewWidth)

        Log.d("dhl", "extentX ${image.extentX}, extentZ ${image.extentZ}")

        val pose = Pose.makeTranslation(0.0f, 0.0f, 0.0f)
        val localPosition = Vector3(pose.tx(), pose.ty(), pose.tz())
        val centerNode = Node() // Looks like this doesn't do anything?
        centerNode.setParent(this) // Notice how centerNode is not being assigned to any identifier; rather, I am assigning a parent to it.
        // In ARCore, that's how we assign AR objects to scene.
        centerNode.localPosition = localPosition
        centerNode.localRotation = Quaternion(pose.qx(), 90f, -90f, pose.qw())
        centerNode.renderable = gifView

        // https://developers.google.com/sceneform/reference/com/google/ar/sceneform/rendering/ViewRenderable#getView()
        gifView?.view?.let { view ->
            Log.i("dhl", "\n\n\n\n\n Attaching image view in AugmentedImageNode.");
            val imageView = view.findViewById<ImageView>(R.id.imageView)

            val imageUri = when (image.name) {
                "tofu_soup" -> Uri.parse("https://media1.giphy.com/media/3ohuAbHlxHTLowDaV2/giphy.gif?cid=ecf05e47fad5086acac0ed0871aeaf9bc867a96d552df629&rid=giphy.gif")
                else -> null
            }

            Glide.with(view.context)
                .load(imageUri)
                .into(imageView)
        }
    }
}