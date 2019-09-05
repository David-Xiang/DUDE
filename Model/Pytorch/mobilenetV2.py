import torch
from torch.autograd import Variable
model = torch.hub.load('pytorch/vision', 'mobilenet_v2', pretrained=True)
model.eval()
input = Variable(torch.randn(1, 3, 224, 224))
torch_out = torch.onnx._export(model, input, "mobilenetV2.onnx", export_params=True)