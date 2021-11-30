let frame_rate = 16;
let secs_per_train = 1;

var video;
let path = "./Videos/";
let videos = [
  ["Barrido_Delante_A.mp4",0],
  ["Codazo_Giro_A.mp4",0],
  ["Codazo_Lateral_B_SHORT.mp4",0],
  ["Cross_B_SHORT.mp4",0],
  ["Defensa_Alta_B_SHORT.mp4",0],
  ["Defensa_Baja_B_SHORT.mp4",0],
  ["Defensa_Codazo_Delante_M_S.mp4",0],
  ["Defensa_Codazo_Lateral_m.mp4",0],
  ["Defensa_Media_B_S.mp4",0],
  ["Esquivar_B_S.mp4",0],
  ["Guardia_b.mp4",0],
  ["Jav_B_S.mp4",0],
  ["Jav_Cross_B_S.mp4",0],
  ["Lateral_Rodilla_M_S.mp4",0],
  ["Rodillazo_B_S.mp4",0],
  ["Soltar_Agarre_Mano_Fuera_B_S_2.mp4",0],
  ["Soltar_Mano_Agarre_Dentro_M_S_3.mp4",0],
  ["braceadas_defensivas1.mp4",0],
  ]
var videoName;
var videoSelected;
var videoSelector;
let playing = false;

let times = {};
var videoTimes = [];

/*let width = 640;  
let height = 360;*/
let width = 360;  
let height = 640;

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

var videoTimeLabel;
var savedTimes;

function setup() {

  var canvas = createCanvas(650, 650);
  canvas.center("horizontal");
  setFrameRate(frame_rate);

  videoTimeLabel = document.getElementById("videoTimeLabel");
  savedTimes = document.getElementById("savedTimes");

  video = createVideo();
  video.size(height, width);
  video.volume(0);
  video.noLoop();
  video.hide();

  setOptions();

  videoSelector.onchange = function(){
    videoSelected = videoSelector.value
    videoName = videos[ videoSelected ][0]
    videoTimes = [];
    video.src = path+videoName;
    reCreateBrain();
  };

  buttonPlay = select("#playButton");
  buttonPlay.mousePressed(toggleVid);

  buttonSave = select("#saveButton");
  buttonSave.mousePressed(saveVideo);

  buttonToggleDimensions = select("#toggleDimensionsButton");
  buttonToggleDimensions.mousePressed(toggleDimensions);

  minusSpeedButton = select("#minusSpeedButton");
  minusSpeedButton.mousePressed(minusSpeed);

  plusSpeedButton = select("#plusSpeedButton");
  plusSpeedButton.mousePressed(plusSpeed);

  restartButton = select("#restartButton");
  restartButton.mousePressed(restartVideo);

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

  video.onended( videoEnded );
}

function toggleDimensions(){
  if(width == 640){
    width = 360;
    height = 640;
  }else{
    width = 640;
    height = 360;
  }
  video.size(height, width);
  clear();
}

function setOptions(){

  var setted = false
  videoSelector = document.getElementById("video-dd");
  var option;
  var il = videos.length;

  for(i = 0; i < il; i += 1) {
      option = document.createElement('option');
      option.setAttribute('value', i);
      if( !setted && videos[i][1] == 0 ){
        option.setAttribute("selected", true);
        setted = true
      }
      option.appendChild(document.createTextNode(videos[i][0]+(videos[i][1] ? "-DONE"  : "")));
      videoSelector.appendChild(option);
  }

  videoSelected = videoSelector.value
  videoName = videos[ videoSelected ][0]
  videoTimes = [];
  video.src = path+videoName;
  video.size(height, width);
  video.volume(0);
  video.noLoop();

  reCreateBrain();
}

function reCreateBrain(){

  let options = {
      inputs: 34,
      outputs: Math.ceil(video.duration()),
      task: 'classification',
      data: true
    }
  brain = ml5.neuralNetwork(options);
}

function videoEnded(){

  saveVideo();

  playing = !playing;
  buttonPlay.html('play');
  frameCount = 0;

  videos[ videoSelected ][1] = true;
  setOptions();

  reCreateBrain();
}

function plusSpeed() {
  video.speed(video.speed()+0.25);
}

function minusSpeed() {
  video.speed(video.speed()-0.25);
}

function restartVideo() {
  video.time(0);
  reCreateBrain();
  
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

function saveVideo() {
  times[videoName] = videoTimes;
  brain.saveData(videoName+"-DATA");
}

function draw() {
  let frame = video.get()
  image(frame, 0, 0);

  videoTimeLabel.innerHTML = video.time().toFixed(2);
  savedTimes.innerHTML = videoTimes.join(",\n");
  
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
    if (poses[i].pose.score > 0.01) {
      // For each pose detected, loop through all the keypoints
      for (let j = 0; j < poses[i].pose.keypoints.length; j++) {
        // A keypoint is an object describing a body part (like rightArm or leftShoulder)
        let keypoint = poses[i].pose.keypoints[j];
        // Only draw an ellipse is the pose probability is bigger than 0.2
      
        // Draw
        fill(0, 255, 255);
        noStroke();
        ellipse(keypoint.position.x, keypoint.position.y, 5, 5);
      }
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

//function keyPressed() for p, spacebar and s p5.js
function keyPressed() {
  if (keyCode === 32) { // spacebar
    toggleVid();
    var inputs = [];
    for (let i = 0; i < poses.length; i++) {
      if (poses[i].pose.score > 0.01) {
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
          
          inputs.push(keypoint.position.x);
          inputs.push(keypoint.position.y);
        }
      }
      let target = [String((video.time()*1000).toFixed(0))];
      brain.addData(inputs, target);
      videoTimes.push((video.time()*1000).toFixed(0));
    }
    toggleVid();
  }
  if (keyCode === 80) { // p
    toggleVid();
  }
  if (keyCode === 83) { // s
    saveVideo();
  }
}

