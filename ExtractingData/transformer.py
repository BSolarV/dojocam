import sys
import tensorflow as tf

modelPath = sys.argv[1]+"-keras"

converter = tf.lite.TFLiteConverter.from_saved_model(modelPath)
tflite_model = converter.convert()
# Save the model.
modelPath = modelPath.replace("-keras", "").split("/")[-1]
with open(f'tflite-models/{modelPath}.tflite', 'wb') as f:
  f.write(tflite_model)
