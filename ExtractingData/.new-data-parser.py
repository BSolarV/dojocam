import json
import sys
import numpy as np

modelPath = sys.argv[1]
modeName = modelPath.split('/')[-1]
videoName = modeName.split("-")[0]
jsonPath = modelPath
jsonFile = open(jsonPath, "r")
jsonData = json.load(jsonFile)
jsonFile.close()

targetKeypoints = {}
for data in jsonData["data"]:
	target = data["ys"]["output0"]
	keypoints = list(data["xs"].items())

	if( target not in targetKeypoints ):
		targetKeypoints[target] = [ 0 for i in range(34) ]

	for i in range(len(keypoints)):
		targetKeypoints[target][i] = keypoints[i][1] 



with open(f'models-data/{videoName}.csv', 'w') as fp:
  fp.write("\n".join( [ f"{target},"+",".join(list(map(str,values))) for target,values in list(targetKeypoints.items()) ] ))
with open(f'models-data/{videoName}-labels.txt', 'w') as fp:
  fp.write( "\n".join( list( targetKeypoints.keys() ) ) )