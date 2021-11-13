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

package org.tensorflow.lite.examples.poseestimation.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.examples.poseestimation.VisualizationUtils
import org.tensorflow.lite.examples.poseestimation.data.Person
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.examples.poseestimation.data.BodyPart
import org.tensorflow.lite.examples.poseestimation.data.KeyPoint
import java.lang.Exception
import kotlin.math.floor


class PoseClassifier(
    private val interpreter: Interpreter,
    private val labels: List<String>
) {
    private val input = interpreter.getInputTensor(0).shape()
    private val output = interpreter.getOutputTensor(0).shape()

    companion object {
        private var MODEL_FILENAME: String = ""
        private var LABELS_FILENAME: String = ""
        private var EXPECTED_POSE_FILENAME: String = ""
        private const val CPU_NUM_THREADS = 4

        var expectedBodies = HashMap<String, Person>()

        fun create(context: Context, namefile: String) : PoseClassifier {
            val options = Interpreter.Options().apply {
                setNumThreads(CPU_NUM_THREADS)
            }
            MODEL_FILENAME = "tflite-models/$namefile.tflite"
            LABELS_FILENAME = "tflite-models/$namefile-labels.txt"
            EXPECTED_POSE_FILENAME = "models-data/$namefile.csv"

            try {
                val bufferReader = context.assets.open(EXPECTED_POSE_FILENAME).bufferedReader()
                while (bufferReader.ready()) {
                    val line: String = bufferReader.readLine()
                    val parts = line.split(",").toTypedArray()
                    val target = parts[0]
                    val keyPoints = mutableListOf<KeyPoint>()
                    var i = 0;
                    while ( i < 34 ){
                        val keyPoint = KeyPoint( BodyPart.fromInt(floor(((i/2).toDouble())).toInt()), PointF(parts[i+1].toFloat(),parts[i+2].toFloat()), 1f )
                        keyPoints.add(keyPoint)
                        i+=2
                    }
                    val person = Person(keyPoints, 1f)
                    expectedBodies[target] = person
                }
                bufferReader.close()
            } catch (e: Exception) {
                e.message?.let { Log.i("Error", it) }
            }

            return PoseClassifier(
                Interpreter(
                    FileUtil.loadMappedFile(
                        context, MODEL_FILENAME
                    ), options
                ),
                FileUtil.loadLabels(context, LABELS_FILENAME)
            )
        }
        fun getExpectedBody(target: String) : Person? {
            return expectedBodies[target]
        }
    }

    fun classify(person: Person?): List<Pair<String, Float>> {
        // Preprocess the pose estimation result to a flat array
        val inputVector = FloatArray(input[1])
        person?.keyPoints?.forEachIndexed { index, keyPoint ->
            inputVector[index * 2] = keyPoint.coordinate.x
            inputVector[index * 2 + 1] = keyPoint.coordinate.y
            //inputVector[index * 3 + 2] = keyPoint.score
        }

        // Postprocess the model output to human readable class names
        val outputTensor = FloatArray(output[1])
        interpreter.run(arrayOf(inputVector), arrayOf(outputTensor))
        val output = mutableListOf<Pair<String, Float>>()
        outputTensor.forEachIndexed { index, score ->
            output.add(Pair(labels[index], score))
        }
        return output
    }

    fun close() {
        interpreter.close()
    }

    fun drawExpectedBody(bitmap: Bitmap, target: String, person: Person): Bitmap {
        var outputBitmap = bitmap
        getExpectedBody(target)?.let {
            it.repositionFromPerson(person)
            outputBitmap = VisualizationUtils.drawExpectedBodyKeypoints(
                bitmap,
                it
            )
        }
        return outputBitmap
    }

}
