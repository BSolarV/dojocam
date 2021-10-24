let frame_rate = 16;
let frame_rate_label;

var video;
let path = "";
let videoName = 'braceadas_defensivas1';
let extension = ".mp4";
let playing = false;

/*let width = 640;	
let height =360;*/

let width = 360;  
let height= 640;

let poseNet;
let poses = [];
let skeletons = [];
var imageScaleFactor= 1;
var outputStride=16 ;
var minConfidence = 0.5;
var maxPoseDetections = 1;
var scoreThreshold = 0.5 ;
var multiplier = 0.75;

let brain;
let frameCount = 0;
let max_frame = 0;//modificable según frames del video

let running = false;

var state = 'waiting';

function setup() {

  createCanvas(height, width+10);
  setFrameRate(frame_rate);
  frame_rate_label = select("#FrameRate");

  video = createVideo(path+videoName+extension);
  video.size(height, width);
  video.volume(0);
  video.noLoop();
  video.hide();

  button = select("#playButton");
  button.mousePressed(toggleVid); 

  //Posenet
  poseNet = ml5.poseNet(
      video, 
      {
        imageScaleFactor: 0.3,
        outputStride: 16,
        flipHorizontal: false,
        minConfidence: 0.5,
        maxPoseDetections: 1,
        scoreThreshold: 0.5,
        nmsRadius: 20,
        detectionType: 'multiple',
        inputResolution: 513,
        multiplier: 0.75,
        quantBytes: 2,
      },
      modelReady
    );

  poseNet.on('pose', function (results) {
    poses = results;
    });


  let options = {
      inputs: 34,
      outputs: Math.ceil(video.duration()),
      task: 'classification',
      data: true
    }
  brain = ml5.neuralNetwork(options);

  video.onended( trainAndSave )

}

// plays or pauses the video depending on current state
function toggleVid() {
  if (playing) {
    video.pause();
    button.html('play');
  } else {
    video.play();
    button.html('pause');
  }
  playing = !playing;
}

function modelReady() {
  console.log('model ready');
}

function trainAndSave() {
  button.html('pause');
  playing = !playing;

  brain.saveData(videoName+"-DATA");

  // Training
  brain.normalizeData();
  brain.train({epochs: 100}, finished);

}

function finished() {
  console.log("Model Trained");
  let modelName = videoName+"-model";
  console.log(" "+modelName);
  brain.save(modelName);
}

function draw() {
  let frame = video.get()
  image(frame, 0, 0);
  if( playing ) {
    frameCount++;
  }
  frame_rate_label.elt.innerText = Math.round(video.time())  + " - Frame: " + frameCount;

  // We can call both functions to draw all keypoints and the skeletons
  drawKeypoints();
  drawSkeleton();
}
//=======Draw skeleton and keypoints ===/

// A function to draw ellipses over the detected keypoints
function drawKeypoints()  {
	let inputs = [];
  // Loop through all the poses detected
  for (let i = 0; i < poses.length; i++) {
    if (poses[i].pose.score > 0.5) {
      // For each pose detected, loop through all the keypoints
      for (let j = 0; j < poses[i].pose.keypoints.length; j++) {
        // A keypoint is an object describing a body part (like rightArm or leftShoulder)
        let keypoint = poses[i].pose.keypoints[j];
        // Only draw an ellipse is the pose probability is bigger than 0.2
      
        // Draw
        fill(0, 255, 255);
        noStroke();
        ellipse(keypoint.position.x, keypoint.position.y, 5, 5);

        // Save for Training
        if( playing ){
          inputs.push(keypoint.position.x);
          inputs.push(keypoint.position.y);
        }
      }
    }
    if( playing ){
      let target = [Math.round(video.time())];
      brain.addData(inputs, target);
    }
  }
}

// A function to draw the skeletons
function drawSkeleton() {
  // Loop through all the skeletons detected
  for (let i = 0; i < poses.length; i++) {
    // For every skeleton, loop through all body connections
    for (let j = 0; j < poses[i].skeleton.length; j++) {
      let partA = poses[i].skeleton[j][0];
      let partB = poses[i].skeleton[j][1];
      stroke(255, 0, 0);
      line(partA.position.x, partA.position.y, partB.position.x, partB.position.y);
    }
  }
}

