let frame_rate = 16;
let frame_rate_label;
let secs_per_train = 3;

var video;
let path = "";
let videoName = 'Barrido_Delante_A';
let extension = ".mp4";
let playing = false;

let width = 640;  
let height = 360;

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

  var canvas = createCanvas(height, width+10);
  canvas.center("horizontal");
  setFrameRate(frame_rate);
  frame_rate_label = select("#FrameRate");

  video = createVideo(path+videoName+extension);
  video.size(height, width);
  video.volume(0);
  video.noLoop();
  video.hide();

  buttonPlay = select("#playButton");
  buttonPlay.mousePressed(toggleVid);

  buttonSave = select("#saveButton");
  buttonSave.elt.disabled = 1;
  buttonSave.mousePressed(saveBrain);

  buttonCheck = select("#checkButton");
  buttonCheck.elt.disabled = 1;
  buttonCheck.mousePressed(checkActualPose);

  logChecked = select("#logChecked");
  logChecked.elt.disabled = 1;
  logChecked.mousePressed(showActualPose);

  buttonCustomCheck = select("#customCheckButton");
  buttonCustomCheck.elt.disabled = 1;
  buttonCustomCheck.mousePressed(checkCustomPose);

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

  video.onended( videoEnded );

}

function videoEnded(){

  playing = !playing;
  buttonPlay.html('play');
  frameCount = 0;

  // Training
  //brain.loadData('Barrido_Delante_A-DATA.json');
  brain.normalizeData();
  brain.train({epochs: 20});
  console.log("Model Trained");

  logChecked.elt.disabled = 0;
  buttonSave.elt.disabled = 0;
  buttonCheck.elt.disabled = 0;
  buttonCustomCheck.elt.disabled = 0;
}

// plays or pauses the video depending on current state
function toggleVid() {
  if (playing) {
    video.pause();
    buttonPlay.html('play');
  } else {
    video.play();
    buttonPlay.html('pause');
  }
  playing = !playing;
}

function modelReady() {
  console.log('model ready');
}

function saveBrain() {
  brain.saveData(videoName+"-DATA");
  let modelName = videoName+"-model";
  brain.save(modelName);
}

function draw() {
  let frame = video.get()
  image(frame, 0, 0);
  if( playing ) {
    frameCount++;
  }
  frame_rate_label.elt.innerText = Math.floor(video.time())  + " - Frame: " + frameCount;

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
      let target = [String(Math.floor(video.time()/secs_per_train)*secs_per_train)];
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

function gotResults(error, result){ 
  console.log(result); 
  console.log(error);
}


function checkActualPose() {
  let inputs = [];
  // Loop through all the poses detected
  for (let i = 0; i < poses.length; i++) {
    if (poses[i].pose.score > 0.5) {
      // For each pose detected, loop through all the keypoints
      for (let j = 0; j < poses[i].pose.keypoints.length; j++) {
        // A keypoint is an object describing a body part (like rightArm or leftShoulder)
        let keypoint = poses[i].pose.keypoints[j];

        // Save for checking
        inputs.push(keypoint.position.x);
        inputs.push(keypoint.position.y);
      }
    }
  }
  console.log(brain);
  console.log(inputs);
  console.log(classifyed);
  brain.classify(inputs, classifyed);
}

function classifyed(error, results) {
    console.log(results);
    console.log(error);
    poseLabeled = select("#poseLabeled");
    poseLabeled.elt.innerText = results[0].label;
}

function showActualPose() {
  let inputs = [];
  // Loop through all the poses detected
  for (let i = 0; i < poses.length; i++) {
    if (poses[i].pose.score > 0.5) {
      // For each pose detected, loop through all the keypoints
      for (let j = 0; j < poses[i].pose.keypoints.length; j++) {
        // A keypoint is an object describing a body part (like rightArm or leftShoulder)
        let keypoint = poses[i].pose.keypoints[j];

        // Save for checking
        inputs.push(keypoint.position.x);
        inputs.push(keypoint.position.y);
      }
    }
  }
  console.log(inputs);
}

function checkCustomPose() {
  console.log(results);
  console.log(error);
  if( l != null || l != [] ){
    brain.classify(l, classifyedCustom);
  } else {
    console.log("");
    console.log("Error:");
    console.log(l);
    console.log("");
  }
}

function classifyedCustom(error, results) {
    poseLabeled = select("#customPoseLabeled");
    poseLabeled.elt.innerText = results[0].label;
    console.log(results);
}