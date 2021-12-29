for d in NewModels/* ; do
    echo $d
    python3 .new-data-parser.py $d
done
