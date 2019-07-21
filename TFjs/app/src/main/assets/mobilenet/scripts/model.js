'use strict'
/*
author: David Xiang
email: xdw@pku.edu.cn
 */

let model;
let warmupTime="cpu", inferTime=0;

async function initData(){
    let offset = tf.scalar(127.5);
    let data = [];
    for (let i = 0; i < DATASIZE; i++){
        // load img from a canvas
        let img = document.getElementById("pic"+i);
        let dataItem = tf.fromPixels(img).
                        toFloat().
                        sub(offset).
                        div(offset).
                        reshape([1, picSize, picSize, 3]);
        data.push(dataItem);
    }
    return data;
}
async function initModel(){
    //set backend
    if (backend == "cpu")
        tf.setBackend("cpu");
    else
        tf.setBackend("webgl");

    // load model
    if (verbose){
        console.log(tf.getBackend());
        console.log("loading model");
    }

    statusLog("Loading Model");
    //TODO!!!
    //model = await tf.loadModel("file:///android_asset/mobile/model.json");

    // warm up the model
    // statusLog("Warming up");
        
    // if (backend == "gpu"){
    //     let begin = new Date();
    //     model.predict(tf.zeros([1, 224, 224, 3])).dispose();
    //     let end = new Date();
    //     warmupTime = end - begin;
    // }
}

async function infer(data){
    statusLog("Inferring");

    let round = 0;
    while(inferTime < totTime){
        if (verbose)
            console.log("Case" + round);

        let index = round % DATASIZE;

        let begin = new Date();
        model.predict(data[index]);
        let end = new Date();
        inferTime += end - begin;
        round++;
    }

    //triggerEnd(task + warmupTime + "\t" + inferTime/round);
}

async function init(){
    loadPic(); // defined in data.js
    await initModel(); 

    let data = initData();

    statusLog("Ready");
    return data;
}


async function main(){
    let argsStatus = parseArgs(); // defined in params.js
    if (argsStatus == false)
        return;

    let data = await init();
    await infer(data);
    statusLog("Finished");
}
main();

