ls *.onnx | xargs -I {} basename {} .onnx | xargs -I {} ./MNNConvert -f ONNX --modelFile {}.onnx --MNNModel {}.mnn --bizCode MNN
