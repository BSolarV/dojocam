for d in Models/* ; do
    #./.transform.sh $d
    #python3 .data-parser.py $d
    echo $d
done

#./.clean-keras.sh