import scipy.io
import numpy as np

meta = scipy.io.loadmat("meta.mat")
original_idx_to_synset = {}
synset_to_name = {}

for i in range(1000):
    ilsvrc2012_id = int(meta["synsets"][i,0][0][0][0])
    synset = meta["synsets"][i,0][1][0]
    name = meta["synsets"][i,0][2][0]
    original_idx_to_synset[ilsvrc2012_id] = synset
    synset_to_name[synset] = name

synset_to_keras_idx = {}
keras_idx_to_name = {}
f = open("synset_words.txt","r")
idx = 0
for line in f:
    parts = line.split(" ")
    synset_to_keras_idx[parts[0]] = idx
    keras_idx_to_name[idx] = " ".join(parts[1:])
    idx += 1
f.close()

def convert_original_idx_to_keras_idx(idx):
    return synset_to_keras_idx[original_idx_to_synset[idx]]

fin = open("ILSVRC2012_validation_ground_truth.txt","r")
y_val = fin.read().strip().split("\n")
y_val = list(map(int, y_val))
y_val = [convert_original_idx_to_keras_idx(idx) for idx in y_val]
fin.close()

fout = open("ILSVRC2012_validation_ground_truth_mapped.txt", "w")
for i in y_val:
    fout.write(str(i+1)+"\n")
fout.close()