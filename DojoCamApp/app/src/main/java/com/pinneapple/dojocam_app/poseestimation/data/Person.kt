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
import java.lang.Double.NaN
import kotlin.math.pow
import kotlin.math.roundToInt

data class Person(val keyPoints: List<KeyPoint>, val score: Float){
    fun repositionFromPerson(person: Person){
        val leftShoulder = keyPoints[5].coordinate
        val rightShoulder = keyPoints[6].coordinate

        val leftShoulderPerson = person.keyPoints[5].coordinate
        val rightShoulderPerson = person.keyPoints[6].coordinate

        // Scaling
        val distance = ( (leftShoulder.x-rightShoulder.x).toDouble().pow(2) + (leftShoulder.y-rightShoulder.y).toDouble().pow(2) ).toDouble().pow(0.5)
        val distancePerson = ( (leftShoulderPerson.x-rightShoulderPerson.x).toDouble().pow(2) + (leftShoulderPerson.y-rightShoulderPerson.y).toDouble().pow(2) ).toDouble().pow(0.5)
        val factor = distancePerson/distance

        leftShoulder.x = (leftShoulder.x * factor).toFloat()
        leftShoulder.y = (leftShoulder.y * factor).toFloat()

        // Moving
        val xDistance = leftShoulderPerson.x - leftShoulder.x
        val yDistance = leftShoulderPerson.y - leftShoulder.y

        leftShoulder.x = leftShoulder.x + xDistance
        leftShoulder.y = leftShoulder.y + yDistance

        // Every Other Keypoint
        for (kp in keyPoints){
            if (kp == keyPoints[5]){
                continue
            }
            kp.coordinate.x = (kp.coordinate.x * factor).toFloat() + xDistance
            kp.coordinate.y = (kp.coordinate.y * factor).toFloat() + yDistance
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
