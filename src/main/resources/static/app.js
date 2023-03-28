function init() {
    document.getElementById("prompt").addEventListener("keyup", function (event) {
        if (event.key === "Enter") {
            return generateImage();
        }
        return false;
    });
}

function generateImage(e) {
    console.log("Generating image");
    document.getElementById("loading").style.display = "inline";
    const prompt = document.getElementById("prompt").value;
    document.getElementById("image").style.display = "none";
    document.getElementById("prompt").disabled = true;
    document.getElementById("button").style.visibility = "hidden";
    window.getSelection().removeAllRanges();
    axios.get("/api/v1/image?prompt=" + prompt)
        .catch(error => {
            document.getElementById("image").style.display = "none";
        }).then(res => {
        console.log("Got image: " + res.data.url);
        document.getElementById("image").src = res.data.url;
        document.getElementById("image").style.display = "block";
    }).finally(() => {
        document.getElementById("loading").style.display = "none";
        document.getElementById("prompt").disabled = false;
        document.getElementById("button").style.visibility = "visible";
        document.getElementById("prompt").focus();
        document.getElementById("prompt").select();
    });
    return true;
};
