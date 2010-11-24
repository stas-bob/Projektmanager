var xmlHttp;

function createXMLHttpRequest() {
    if (window.ActiveXObject) {
        xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
        }
    else if(window.XMLHttpRequest) {
        xmlHttp = new XMLHttpRequest();
        }
 }

function validateProjectServlet()
{
    var servlet = "/Projektmanager/ValideProjectServlet";
    servlet += "?projectname=" + document.getElementById('projectname').value;
    createXMLHttpRequest();
    xmlHttp.open('POST',servlet, true);
    xmlHttp.onreadystatechange = callbackValidateProject;
    xmlHttp.send(null);
}

function validateEmailServlet()
{
    var servlet = "/Projektmanager/ValidateEmailServlet";
    servlet += "?email=" + document.getElementById('email').value;
    createXMLHttpRequest();
    xmlHttp.open('POST',servlet, true);
    xmlHttp.onreadystatechange = callbackValidateEmail;
    xmlHttp.send(null);
}

function startAsync(servlet)
{
    createXMLHttpRequest();
    xmlHttp.open('POST',servlet, true);
    xmlHttp.onreadystatechange = callback;
    xmlHttp.send(null);
}

function callbackValidateProject() {
        if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var status = xmlHttp.responseText;
            if (status == "0") {
                 document.getElementById('statusProjectname').innerHTML = "cool";
            } else {
                document.getElementById('statusProjectname').innerHTML = "gibts schon";
            }
       } else {
           document.getElementById('statusProjectname').innerHTML = "Irgendwas stimmt bei Ihne net!";
       }
    }
}

function callbackValidateEmail() {
        if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var status = xmlHttp.responseText;
            if (status == "0") {
                 document.getElementById('statusEmail').innerHTML = "cool";
            } else {
                document.getElementById('statusEmail').innerHTML = "gibts schon";
            }
       } else {
           document.getElementById('statusEmail').innerHTML = "Irgendwas stimmt bei Ihne net!";
       }
    }
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

function printWait(id) {
    document.getElementById(id).innerHTML = "bitte warten ...";
}

function callback()
{
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            document.write("<a href='Login.html'>ok hat gang</a>");
       } else {
             document.getElementById("statusSubmit").innerHTML = "Es gab einen Fehler";
       }
    }
}

