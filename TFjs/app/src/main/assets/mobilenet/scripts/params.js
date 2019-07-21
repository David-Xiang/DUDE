'use strict'
/*
author: David Xiang
email: xdw@pku.edu.cn
 */

// constant args
const DATASIZE = 10;

// args to be extracted from url
let backend;
let totTime;
let task;// = like "Tfjs\tres50\tcpu\t10000\t";
let verbose = true;
let picSize = 299;

let backendList = ["cpu", "gpu"];

function getParam(query, key){
    let regex = new RegExp(key+"=([^&]*)","i");
    return query.match(regex)[1];
}


function parseArgs(){
    let address = document.location.href;
    let query = address.split("?")[1];

    backend = getParam(query, "backend");
    totTime = parseInt(getParam(query, "processtime"));

    // check whether these params are valid
    if (backendList.indexOf(backend) === -1){
        triggerStart();
        console.error("Invalid URI:" + address);
        return false;
    }

    if (totTime <= 0){
        triggerStart();
        console.error("Invalid URI:" + address);
        return false;
    }
    // get right task name
    task = "tfjs\tinference\tkeras\t" + modelName + "\t" + backend + "\t";
    document.getElementById("task").innerText = task;
    return true;
}