/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package org.tensorflow.lite.examples.poseestimation.data

import android.util.Log
import org.tensorflow.lite.examples.poseestimation.camera.CameraSource
import kotlin.math.pow
import kotlin.math.roundToInt

data class Person(val keyPoints: List<KeyPoint>, val score: Float){
    fun repositionFromPerson(person: Person){
        val leftShoulder = keyPoints[5].coordinate
        val rightShoulder = keyPoints[6].coordinate
        val leftHip = keyPoints[11].coordinate

        val leftShoulderPerson = person.keyPoints[5].coordinate
        val rightShoulderPerson = person.keyPoints[6].coordinate
        val leftHipPerson = person.keyPoints[11].coordinate

        // Scaling
        val distanceX = ( (leftShoulder.x-rightShoulder.x).toDouble().pow(2) + (leftShoulder.y-rightShoulder.y).toDouble().pow(2) ).pow(0.5)
        val distancePersonX = ( (leftShoulderPerson.x-rightShoulderPerson.x).toDouble().pow(2) + (leftShoulderPerson.y-rightShoulderPerson.y).toDouble().pow(2) ).pow(0.5)
        val factorX = distancePersonX/distanceX

        val distanceY = ( (leftShoulder.x-leftHip.x).toDouble().pow(2) + (leftShoulder.y-leftHip.y).toDouble().pow(2) ).pow(0.5)
        val distancePersonY = ( (leftShoulderPerson.x-leftHipPerson.x).toDouble().pow(2) + (leftShoulderPerson.y-leftHipPerson.y).toDouble().pow(2) ).pow(0.5)
        val factorY = distancePersonY/distanceY

        leftShoulder.x = (leftShoulder.x * factorX).toFloat()
        leftShoulder.y = (leftShoulder.y * factorY).toFloat()

        // Moving
        val xDistance = leftShoulderPerson.x - leftShoulder.x
        val yDistance = leftShoulderPerson.y - leftShoulder.y

        leftShoulder.x = leftShoulder.x + xDistance
        leftShoulder.y = leftShoulder.y + yDistance

/*
        val distCheck = ( ( rightShoulderPerson.x - rightShoulder.x ).pow(2) + ( rightShoulderPerson.y - rightShoulder.y ).pow(2) ).pow(0.5f)
        val midPoint = CameraSource.PREVIEW_WIDTH / 2
        val flippedRightShoulderX = midPoint - ( rightShoulder.x - midPoint )
        val newDist = ( ( rightShoulderPerson.x - flippedRightShoulderX ).pow(2) + ( rightShoulderPerson.y - rightShoulder.y ).pow(2) ).pow(0.5f)
        if( newDist < distCheck ){
            for (kp in keyPoints){
                if (kp == keyPoints[5]){
                    continue
                }
                kp.coordinate.x =  midPoint - ( kp.coordinate.x - midPoint )
            }
        }
        */

        // Every Other Keypoint
        for (kp in keyPoints){
            if (kp == keyPoints[5] ){
                continue
            }
            kp.coordinate.x = (kp.coordinate.x * factorX).toFloat() + xDistance
            kp.coordinate.y = (kp.coordinate.y * factorY).toFloat() + yDistance
        }


    }

    fun getDiference(person: Person): Int {
        var diference = 0f
        // Every Other Keypoint
        //Log.wtf("AAAAAAAAAAAAAAAAAAAAAA", diference.pow(0.5f).roundToInt().toString())
        var i = 0
        while( i<keyPoints.size ){
            val thisKP = keyPoints[i].coordinate
            val personKP = person.keyPoints[i].coordinate
            val distance = ( ( thisKP.x - personKP.x ).pow(2) + ( thisKP.y - personKP.y ).pow(2) )
            diference += distance
            i++
        }
        if(diference.pow(0.5f).isNaN()){
            Log.wtf("BBBBBBBBBBBBBBBBBBBB", "NaN")
            return 1000
        }
        Log.wtf("BBBBBBBBBBBBBBBBBBBB", diference.pow(0.5f).roundToInt().toString())
        return diference.pow(0.5f).roundToInt()
    }
}
