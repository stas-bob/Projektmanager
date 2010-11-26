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
            + "<td>Name:<input type=\"text\" id=\"name\"/></td>"
            + "</tr>"
            + "<tr>"
            + "<td>Email:<input type=\"text\" onblur=\"printWait('statusEmail');validateEmailServlet()\" id=\"email\"/></td><td><div id=\"imgEmail\"></div></td><td><div id=\"statusEmail\"></div></td>"
            + "</tr>"
            + "<tr>"
            + "<td><input id=\"button\" type=\"button\" value=\"Speichern\" onclick=\"saveUser()\"/></td><td><input type=\"button\" value=\"Abbrechen\" onclick=\"hideAddUser()\"/></td>"
            + "</tr>"
            + "</body></html>";
    document.getElementById("addUserField").innerHTML = html;
}

function saveUser() {
    createXMLHttpRequest();
    var name = document.getElementById("name").value;
    var email = document.getElementById("email").value;
    xmlHttp.open('POST',"/Projektmanager/Members?addName=" + name + "&addEmail=" + email, true);
    xmlHttp.onreadystatechange = showMembers;
    xmlHttp.send();
}

function showUserDescription(email) {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Members?userDescription=" + email, true);
    xmlHttp.onreadystatechange = callbackShowUserDescription;
    xmlHttp.send();
}

function changeStatus(status, email) {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Members?changeStatus=" + status + "&email=" + email, true);
    xmlHttp.onreadystatechange = callbackShowUserDescription;
    xmlHttp.send();
}

function callbackShowUserDescription() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            document.getElementById("userDescription").innerHTML = xmlHttp.responseText;
        }
    }
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
                document.getElementById('button').disabled = false;
            } else {
                document.getElementById('imgEmail').innerHTML = "<img src=grafik/error.gif />";
                document.getElementById('statusEmail').innerHTML = "E-Mail schon vergeben!";
                document.getElementById('button').disabled = true;
            }
        } else {
            document.getElementById('statusEmail').innerHTML = "Fehler bei der Valiedierung der E-Mail Adresse";
            document.getElementById('button').disabled = true;
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
                document.getElementById("content").style.height = membersCount*40 + "px";
                //document.getElementById("img-div").style.top=membersCount*40 - 91 + "px";
            } else {
                //document.getElementById("img-div").style.top=349 + "px";
            }
            document.getElementById("content").innerHTML = html[0].childNodes[0].nodeValue;
            //document.getElementById("img-div").style.backgroundImage = "url(grafik/bg-yellow.png)";
            //document.getElementById("img-div").style.height=199 + "px";
            //document.getElementById("img-div").style.left=440 + "px";
            //document.getElementById("img-div").style.width=488 + "px";
            //document.getElementById("img-div").style.position="absolute";
        }
    }
}

function printWait(id) {
    document.getElementById(id).innerHTML = "Bitte warten ...";
}

function fillColor(element, color) {
    element.style.backgroundColor = color;
}

function logout() {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Logout", true);
    xmlHttp.onreadystatechange = callbackLogout;
    xmlHttp.send();
}

function callbackLogout()
{
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            document.write(xmlHttp.responseText);
        } else {
            document.write("<a href='Login.html'>Schwerwiegender Fehler.</a>");
        }
    }
}