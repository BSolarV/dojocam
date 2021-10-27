for d in Models/* ; do
    if [[ $d == *"-keras"* ]]; then
       rm -r $d 
    fi
done