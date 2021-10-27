
tensorflowjs_converter --input_format=tfjs_layers_model --output_format=keras_saved_model  ./$1/model.json ./$1-keras

python3 .transformer.py $1

echo done