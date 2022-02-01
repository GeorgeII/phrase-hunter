function lookForPhrase() {
    document.getElementById("message").innerHTML = ""
    document.getElementById('subtitle-list').innerHTML = ""
    // document.getElementById('history-list').innerHTML = ""

    const url = "http://localhost:8080/v1/search"
    const payload = {
        phrase : document.getElementById("phrase").value
    };
    const other_params = {
        headers : { "content-type" : "application/json; charset=UTF-8" },
        body : JSON.stringify(payload),
        method : "POST",
        mode : "cors"
    };

    fetch(url, other_params)
        .then(response => {
            if (response.ok) {
                return response.json()
            } else {
                throw new Error("Could not reach the API: " + response.statusText);
            }
        })
        .then(data => drawResponse(data))
        .catch(error => console.log(error.message));

    return false;
}

function drawResponse(subtitles) {
    if (subtitles.length === 0) {
        document.getElementById("message").innerHTML = "The phrase wasn't found in the video library."
        return
    }

    console.log(subtitles)
    const list = document.getElementById('subtitle-list');
    list.innerHTML = ""

    for (let i = 0; i < subtitles.length; i++) {
        const li = document.createElement('li');
        const textnode = document.createTextNode("Text: " + subtitles[i].text + "\nVideo name: " + subtitles[i].name);
        li.appendChild(textnode);
        list.appendChild(li);
    }




}