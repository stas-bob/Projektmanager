var xmlHttp;

function createXMLHttpRequest() {
    if (window.ActiveXObject) {
        xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else if(window.XMLHttpRequest) {
        xmlHttp = new XMLHttpRequest();
    }
}

function deleteUser(email) {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Members?deleteEmail=" + email, true);
    xmlHttp.onreadystatechange = showMembers;
    xmlHttp.send();
}

function showMembers() {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Members", true);
    xmlHttp.onreadystatechange = callbackMembers;
    xmlHttp.send();
}

function addUser() {
    var html = "<head><head><script src=\"jsXMLHttpRequestHandle.js\" type=\"text/javascript\"></script></head><body><table border=\"0\">"
            + "<tr>"
            + "<td>Name:<input type=\"text\" name=\"name\"/></td>"
            + "</tr>"
            + "<tr>"
            + "<td>Email:<input type=\"text\" name=\"email\"/></td>"
            + "</tr>"
            + "<tr>"
            + "<td><input type=\"button\" value=\"save\"/></td><td><input type=\"button\" value=\"cancel\" onclick=\"hideAddUser()\"/></td>"
            + "</tr>"
            + "</body></html>";
    document.getElementById("addUserField").innerHTML = html;
}
function hideAddUser() {
    document.getElementById("addUserField").innerHTML = "";
}

function validateProjectServlet()
{
    var servlet = "/Projektmanager/ValidateProjectServlet";
    servlet += "?projectname=" + document.getElementById('projectname').value;
    createXMLHttpRequest();
    xmlHttp.open('POST',servlet, true);
    xmlHttp.onreadystatechange = callbackValidateProject;
    xmlHttp.send(null);
}

function callbackValidateProject() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var status = xmlHttp.responseText;
            if (status == "0") {
                document.getElementById('imgProjectname').innerHTML = "<img src=grafik/ok.gif />";
                document.getElementById('statusProjectname').innerHTML = "Projektname noch nicht vergeben!";
            } else {
                document.getElementById('imgProjectname').innerHTML = "<img src=grafik/error.gif />";
                document.getElementById('statusProjectname').innerHTML = "Projektname schon vergeben!";
            }
        } else {
            document.getElementById('statusProjectname').innerHTML = "Fehler bei der Valiedierung des Projektnamens";
        }
    }
}

function validateEmailServlet()
{
    var email = document.getElementById('email').value;
    var emailRegxp =/^.+@.+\..{2,5}$/;

    if(!email.match(emailRegxp)) {
        document.getElementById('imgEmail').innerHTML = "<img src=grafik/error.gif />";
        document.getElementById('statusEmail').innerHTML = "Keine regul&auml;re E-Mail eingegeben!";
        return;
    }

    var servlet = "/Projektmanager/ValidateEmailServlet";
    servlet += "?email=" + email;
    createXMLHttpRequest();
    xmlHttp.open('POST',servlet, true);
    xmlHttp.onreadystatechange = callbackValidateEmail;
    xmlHttp.send(null);
}

function callbackValidateEmail() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var status = xmlHttp.responseText;
            if (status == "0") {
                document.getElementById('imgEmail').innerHTML = "<img src=grafik/ok.gif />";
                document.getElementById('statusEmail').innerHTML = "E-Mail noch nicht vergeben!";
            } else {
                document.getElementById('imgEmail').innerHTML = "<img src=grafik/error.gif />";
                document.getElementById('statusEmail').innerHTML = "E-Mail schon vergeben!";
            }
        } else {
            document.getElementById('statusEmail').innerHTML = "Fehler bei der Valiedierung der E-Mail Adresse";
        }
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

function callback()
{
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            if (xmlHttp.responseText == "0")
                document.write("<a href='Login.html'>Registrierung erfolgreich! Ihnen wurde eine E-Mail zugestellt mit ihren Zugangsdaten.</a>");
            else
                document.getElementById("statusSubmit").innerHTML = "Es gab einen Fehler";
        }
    }
}

function callbackMembers() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var xmlobject = (new DOMParser()).parseFromString(xmlHttp.responseText, "application/xml");
            var html = xmlobject.getElementsByTagName("htmlSeite");
            var membersCount = xmlobject.getElementsByTagName("membersCount")[0].childNodes[0].nodeValue;
            if (membersCount > 10) {
                document.getElementById("content").style.height = membersCount*30;
            }
            document.getElementById("content").innerHTML = html[0].childNodes[0].nodeValue;
        }
    }
}

function printWait(id) {
    document.getElementById(id).innerHTML = "Bitte warten ...";
}

function fillColor(element, color) {
       element.style.backgroundColor = color;
}