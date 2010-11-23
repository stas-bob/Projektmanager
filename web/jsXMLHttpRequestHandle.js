var xmlHttp;

function createXMLHttpRequest() {
    if (window.ActiveXObject) {
        xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
        }
    else if(window.XMLHttpRequest) {
        xmlHttp = new XMLHttpRequest();
        }
 }
 
function getDetail(servlet)
{
    createXMLHttpRequest();
    xmlHttp.open('POST',servlet, true);
    xmlHttp.onreadystatechange = callback;
    xmlHttp.send(null);
}

function buildQuery(servlet) {
    elements = document.getElementsByTagName("input");
    string = servlet + "?";
    i = 0;
    while(elements[i] != null) {
        string += elements[i].getAttribute("name") + "=" + elements[i].value;
        if (elements[i + 1] != null) {
            string += "&";
        }
        i++;

    }
    getDetail(string);
}

function resetStatusText(id) {
    document.getElementById(id).innerHTML = " ";
}

function callback() 
{   
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            document.getElementById("async").innerHTML = xmlHttp.responseText;
            if (xmlHttp.responseText.match(".*backToLogin.*")) {
                window.location = "Login.html";
            }
       }
    }
}

