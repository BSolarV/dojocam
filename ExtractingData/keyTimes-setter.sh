for d in Videos/* ; do
	echo "${d%.*}.old.mp4"
	mv "$d" "${d%.*}.old.mp4"
	ffmpeg -i "${d%.*}.old.mp4" -filter:v fps=30 -vcodec libx264 -x264-params keyint=30:scenecut=0 -acodec copy $d
	rm "${d%.*}.old.mp4"
done