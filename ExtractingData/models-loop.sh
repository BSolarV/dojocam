for d in Models/* ; do
    ./transform.sh $d
done

./clean-keras.sh