file=$1
if [ "${file##*.}" = "onnx" ];then
    echo "filename:${file%.*}"
    ../../../MNN/tools/converter/build/MNNConvert -f ONNX --modelFile "${file%.*}".onnx --MNNModel "${file%.*}".mnn --bizCode MNN
fi