var xmlHttp;

function createXMLHttpRequest() {
    if (window.ActiveXObject) {
        xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
        }
    else if(window.XMLHttpRequest) {
        xmlHttp = new XMLHttpRequest();
        }
 }

function startAsync(servlet)
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
    startAsync(string);
}

function resetStatusText(id) {
    document.getElementById(id).innerHTML = "bitte warten ...";
}

function validateProjectname(servlet) {
    var input = document.getElementById("projektname").value;
    servlet += "?projektname=" + input;
    startAsync(servlet)

}

function callback()
{
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            document.write("<a href=\"Login.html\">toll gemacht. sie sind registriert</a>")
       } else {
           if (xmlHttp.status == 1000) {
               document.getElementById("async").innerHTML = xmlHttp.responseText;
           } else {
               document.getElementById("async").innerHTML = "Irgendwas stimmt bei Ihne net!";
           }
       }
    }
}

