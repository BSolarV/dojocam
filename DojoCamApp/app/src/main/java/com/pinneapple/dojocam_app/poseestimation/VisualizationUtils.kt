//package com.pinneapple.dojocam_app
package org.tensorflow.lite.examples.poseestimation

import android.graphics.*
import org.tensorflow.lite.examples.poseestimation.data.BodyPart
import org.tensorflow.lite.examples.poseestimation.data.Person
import android.graphics.RectF




object VisualizationUtils {
    /** Radius of circle used to draw keypoints.  */
    private const val CIRCLE_RADIUS = 6f

    /** Width of line used to connected two keypoints.  */
    private const val LINE_WIDTH = 4f

    /** Pair of keypoints to draw lines between.  */
    private val bodyJoints = listOf(
        //Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
        //Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
        //Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
        //Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
        //Pair(BodyPart.NOSE, BodyPart.LEFT_SHOULDER),
        //Pair(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    // Draw line and point indicate body pose
    fun drawBodyKeypoints(input: Bitmap, person: Person): Bitmap {

        val paintCircle = Paint().apply {
            strokeWidth = CIRCLE_RADIUS
            color = Color.TRANSPARENT
            style = Paint.Style.FILL
        }
        val paintLine = Paint().apply {
            strokeWidth = LINE_WIDTH
            color = Color.MAGENTA
            style = Paint.Style.FILL
        }

        val output = input.copy(Bitmap.Config.ARGB_8888,true)
        val originalSizeCanvas = Canvas(output)
        bodyJoints.forEach {
            val pointA = person.keyPoints[it.first.position].coordinate
            val pointB = person.keyPoints[it.second.position].coordinate
            originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLine)
        }

        person.keyPoints.forEach { point ->
            originalSizeCanvas.drawCircle(
                point.coordinate.x,
                point.coordinate.y,
                CIRCLE_RADIUS,
                paintCircle
            )
        }
        //System.out.println(output)
        return output
    }

    fun drawExpectedBodyKeypoints(input: Bitmap, person: Person): Bitmap {

        val paintCircle = Paint().apply {
            strokeWidth = CIRCLE_RADIUS
            color = Color.CYAN
            style = Paint.Style.FILL
        }

        val output = input.copy(Bitmap.Config.ARGB_8888,true)
        val originalSizeCanvas = Canvas(output)

        person.keyPoints.forEach { point ->
            originalSizeCanvas.drawCircle(
                point.coordinate.x,
                point.coordinate.y,
                CIRCLE_RADIUS,
                paintCircle
            )
        }
        return output
    }

    fun drawBodyKeypointsError(input: Bitmap, msg: String): Bitmap {

        val size = 20f
        val offset = 8f

        val bgPaint = Paint().apply {
            color = Color.rgb(178,178,178)
            style = Paint.Style.FILL
        }

        val textPaint = Paint().apply {
            strokeWidth = 700f
            color = Color.rgb(82, 0, 129)
            style = Paint.Style.FILL
            textSize = size
            textAlign = Paint.Align.CENTER
        }

        val output = input.copy(Bitmap.Config.ARGB_8888,true)

        val originalSizeCanvas = Canvas(output)
        val rectF = RectF(
            offset,  // left
            offset,  // top
            originalSizeCanvas.width - offset,  // right
            size+2*offset // bottom
        )
        val cornersRadius = 5f
        originalSizeCanvas.drawRoundRect(
            rectF,
            cornersRadius,
            cornersRadius,
            bgPaint
        )
        originalSizeCanvas.drawText(
            msg,
            originalSizeCanvas.width /2f,
            size+offset,
            textPaint
        )
        return output
    }
}