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

async function recognizeImage(path, imageSizeX, imageSizeY, numChannels) {
    let image = new Image();
    image.src = host + path;
    image.onload = async function(){
        let logits = await tf.tidy(() => {
            // tf.browser.fromPixels() returns a Tensor from an image element.
            let img = tf.browser.fromPixels(image, numChannels).toFloat();

            let offset = tf.scalar(127.5);
            // Normalize the image from [0, 255] to [-1, 1].
            let normalized = img.sub(offset).div(offset);

            // Reshape to a single-element batch so we can pass it to predict.
            let batched = null;
            if (numChannels == 1){
                batched = normalized.reshape([1, imageSizeX * imageSizeY]);
            } else {
                batched = normalized.reshape([1, imageSizeX, imageSizeY, numChannels]);
            }

            // Make a prediction through mobilenet.
            return model.predict(batched).arraySync();
        });
        //console.log(logits);
        Android.onAccuracyTaskFinished(logits[0]);
    }
}

async function performanceTask(path, imageSizeX, imageSizeY, numChannels, nSeconds) {
    let image = new Image();
    image.src = host + path;
    image.onload = async function(){
        let round = 0;
        let totTime = 0;
        while(totTime < nSeconds * 1000){
            let begin = new Date();
            let logits = await tf.tidy(() => {
                // tf.browser.fromPixels() returns a Tensor from an image element.
                let img = tf.browser.fromPixels(image, numChannels).toFloat();

                let offset = tf.scalar(127.5);
                // Normalize the image from [0, 255] to [-1, 1].
                let normalized = img.sub(offset).div(offset);

                // Reshape to a single-element batch so we can pass it to predict.
                let batched = null;
                if (numChannels == 1){
                    batched = normalized.reshape([1, imageSizeX * imageSizeY]);
                } else {
                    batched = normalized.reshape([1, imageSizeX, imageSizeY, numChannels]);
                }

                // Make a prediction through mobilenet.
                return model.predict(batched).arraySync();
            });
            let end = new Date();
            totTime += end - begin;
            round++;
            //console.log(round);
            if (round % 100 == 0){
                Android.onProgress(totTime/(nSeconds*10));
            }
        }
        Android.onPerformanceTaskFinished(round, totTime);
    }
}

window.onload = function() {
    Android.onWindowLoaded();
}