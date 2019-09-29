const sleep = (timeout) => new Promise((res, rej) => {
  setTimeout(res, timeout);
});


let model;
let host = "https://astupidmockhost/";

async function loadModelFile(path) {
    model = await tf.loadLayersModel(host + path);
    Android.onModelLoaded();
}

async function setBackend(backend) {
    if (backend != "cpu" && backend != "webgl"){
        console.log("wrong backend: " + backend);
        return;
    }
    await tf.setBackend(backend);
    await tf.enableProdMode();
    Android.onBackendRegistered();
}

async function recognizeImage(path, imageSizeX, imageSizeY) {
    let image = new Image();
    image.src = host + path;
    image.onload = async function(){
        let logits = await tf.tidy(() => {
            // tf.browser.fromPixels() returns a Tensor from an image element.
            const img = tf.browser.fromPixels(image).toFloat();

            const offset = tf.scalar(127.5);
            // Normalize the image from [0, 255] to [-1, 1].
            const normalized = img.sub(offset).div(offset);

            // Reshape to a single-element batch so we can pass it to predict.
            const batched = normalized.reshape([1, imageSizeX, imageSizeY, 3]);

            // Make a prediction through mobilenet.
            return model.predict(batched).arraySync();
        });
        //console.log(logits);
        Android.onInferenceFinished(logits[0]);
    }
}

window.onload = function() {
    Android.onWindowLoaded();
}