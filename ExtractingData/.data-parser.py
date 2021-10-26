import json
import sys
import numpy as np

modelPath = sys.argv[1]
modeName = modelPath.split('/')[-1]
jsonPath = modelPath+f"/{modeName}-DATA.json"
jsonFile = open(jsonPath, "r")
jsonData = json.load(jsonFile)
jsonFile.close()

targetKeypoints = {}
targetCounter = {}
for data in jsonData["data"]:
	target = data["ys"]["output0"]
	keypoints = list(data["xs"].items())
	if( target not in targetKeypoints ):
		targetKeypoints[target] = [ 0 for i in range(34) ]
		targetCounter[target] = 0
	for i in range(len(keypoints)):
		targetKeypoints[target][i] = (targetKeypoints[target][i]*targetCounter[target] + keypoints[i][1] ) / (targetCounter[target]+1)


with open(f'models-data/{modeName}.json', 'w') as fp:
  json.dump(targetKeypoints, fp)
with open(f'tflite-models/{modeName}-labels.txt', 'w') as fp:
  fp.write( "\n".join( list( targetKeypoints.keys() ) ) )