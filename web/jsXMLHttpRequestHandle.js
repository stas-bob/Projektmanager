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

function deleteModule(id) {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Modules?deleteModule=" + id, true);
    xmlHttp.onreadystatechange = showModules;
    xmlHttp.send();
}

function showModules() {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Modules", true);
    xmlHttp.onreadystatechange = callbackModules;
    xmlHttp.send();
}

function addMeToModule(id) {
    document.getElementById("statusBox").innerHTML = "Bitte warten ...";
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Modules?addToModule="+id, true);
    xmlHttp.onreadystatechange = callbackModules;
    xmlHttp.send();
}

function callbackModules() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var xmlobject = (new DOMParser()).parseFromString(xmlHttp.responseText, "application/xml");
            var html = xmlobject.getElementsByTagName("htmlSeite");
            var modulesCount = xmlobject.getElementsByTagName("modulesCount")[0].childNodes[0].nodeValue;
            var error = xmlobject.getElementsByTagName("error")[0].childNodes[0].nodeValue;
            if (error > 0) {
                document.getElementById("statusBox").innerHTML = html[0].childNodes[0].nodeValue;
            } else {
                if (modulesCount > 10) {
                    document.getElementById("content").style.height = modulesCount*40 + "px";
                }
                document.getElementById("content").innerHTML = html[0].childNodes[0].nodeValue;
            }
        }
    }
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
            + "<td>Name:<input type=\"text\" id=\"name\" maxlength=\"40\"/></td>"
            + "</tr>"
            + "<tr>"
            + "<td>Email: <input type=\"text\" onblur=\"printWait('statusEmail');validateEmailServlet()\" id=\"email\"/ maxlength=\"40\"></td><td><div id=\"imgEmail\"></div></td><td><div id=\"statusEmail\"></div></td>"
            + "</tr>"
            + "<tr>"
            + "<td><input id=\"button\" type=\"button\" value=\"Speichern\" onclick=\"saveUser()\"/></td><td><input type=\"button\" value=\"Abbrechen\" onclick=\"hideAddUser()\"/></td>"
            + "</tr>"
            + "</body></html>";
    document.getElementById("addUserField").innerHTML = html;
}

function addMemberToModuleBox() {
    var selectElement = document.getElementById("selectMember");
    var membersInModuleBox = document.getElementById("membersInModuleBox");
    var currentMember = selectElement.options[selectElement.selectedIndex].value;
    var i = 0;
    if (membersInModuleBox.innerHTML.length > 0) {
        while (i < membersInModuleBox.innerHTML.length) {
            if (membersInModuleBox.innerHTML.charAt(i) == '[') {
                var tmpMember = "";
                i++;
                while (membersInModuleBox.innerHTML.charAt(i) != ']') {
                    tmpMember += membersInModuleBox.innerHTML.charAt(i);
                    i++;
                }
                if (currentMember == tmpMember) {
                    return;
                }
            }
            i++;
        }
    }
    membersInModuleBox.innerHTML += "[" + currentMember + "]";
    membersInModuleBox.style.display="block";
}

function removeMemberFromModuleBox() {
    var selectElement = document.getElementById("selectMember");
    var currentMember = selectElement.options[selectElement.selectedIndex].value;
    var membersInModuleBox = document.getElementById("membersInModuleBox");
    var i = 0;
    while (i < membersInModuleBox.innerHTML.length) {
        if (membersInModuleBox.innerHTML.charAt(i) == '[') {
            var fromPos = i;
            var tmpMember = "";
            i++;
            while (membersInModuleBox.innerHTML.charAt(i) != ']') {
                tmpMember += membersInModuleBox.innerHTML.charAt(i);
                i++;
            }
            if (currentMember == tmpMember) {
                membersInModuleBox.innerHTML = membersInModuleBox.innerHTML.substring(0, fromPos) + membersInModuleBox.innerHTML.substring(i + 1);
            }
        }
        i++;
    }
    if (membersInModuleBox.innerHTML.length == 0) {
        membersInModuleBox.style.display="none";
    }


}

function addModule() {
    document.getElementById("addModule").style.border = "1px solid";
    xmlHttp.open('POST',"/Projektmanager/Modules?addModule=true", true);
    xmlHttp.onreadystatechange = callbackAddModule;
    xmlHttp.send();
}

function callbackAddModule() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            document.getElementById("addModule").innerHTML = xmlHttp.responseText;
        }
    }
}

function saveModule() {
    document.getElementById("statusBox").innerHTML = "Bitte warten ...";
    createXMLHttpRequest();
    var name = document.getElementById("name").value;
    var description = document.getElementById("description").value;
    var startDay = document.getElementById("startDay").value;
    var startMonth = document.getElementById("startMonth").value;
    var startYear = document.getElementById("startYear").value;
    var endDay = document.getElementById("endDay").value;
    var endMonth = document.getElementById("endMonth").value;
    var endYear = document.getElementById("endYear").value;
    var prio =  document.getElementById("prio").options[document.getElementById("prio").selectedIndex].value;
    var membersToAdd = document.getElementById("membersInModuleBox").innerHTML;
    var i = 1;
    while (i < description.length) {
        if (i % 40 == 0) {
             description = description.substring(0, i) + '<br>' + description.substr(i);
             last = i + 1;
        }
        i++;
    }
    if (name.length != 0 && description.length != 0 && startDay.length != 0 && startMonth.length != 0 && startYear.length != 0 && endDay.length != 0 && endMonth.length != 0 && endYear.length != 0) {
        var query = "description=" + description + "&" +
                    "name=" + name + "&" +
                    "startDate=" + (startYear.length == 2 ? "20" + startYear : startYear) + "-" + (startMonth.length == 1 ? "0" + startMonth : startMonth) + "-" + (startDay.length == 1 ? "0" + startDay : startDay) + "&" +
                    "endDate=" + (endYear.length == 2 ? "20" + endYear : endYear) + "-" + (endMonth.length == 1 ? "0" + endMonth : endMonth) + "-" + (endDay.length == 1 ? "0" + endDay : endDay) + "&" +
                    "membersToAdd=" + membersToAdd + "&" +
                    "prio=" + prio;

        xmlHttp.open('POST',"/Projektmanager/Modules?" + query, true);
        xmlHttp.onreadystatechange = callbackModules;
        xmlHttp.send();
    }
}

function saveUser() {
    createXMLHttpRequest();
    var name = document.getElementById("name").value;
    var email = document.getElementById("email").value;
    if (name.length != 0 && email.length != 0) {
        xmlHttp.open('POST',"/Projektmanager/Members?addName=" + name + "&addEmail=" + email, true);
        xmlHttp.onreadystatechange = showMembers;
        xmlHttp.send();
    }
}

function showUserDescription(email) {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Members?userDescription=" + email, true);
    xmlHttp.onreadystatechange = callbackShowUserDescription;
    xmlHttp.send();
}
function showModuleDescription(id) {
    document.getElementById("addModule").style.border = "0px solid";
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Modules?moduleDescriptionId=" + id, true);
    xmlHttp.onreadystatechange = callbackShowModuleDescription;
    xmlHttp.send();
}

function changeModuleStatus(status, id) {
    document.getElementById("addModule").innerHTML = "";
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Modules?changeStatus=" + status + "&id=" + id, true);
    xmlHttp.onreadystatechange = callbackShowModuleDescription;
    xmlHttp.send();
}

function changeStatus(status, email) {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Members?changeStatus=" + status + "&email=" + email, true);
    xmlHttp.onreadystatechange = callbackShowUserDescription;
    xmlHttp.send();
}
function callbackShowModuleDescription() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            document.getElementById("addModule").innerHTML = xmlHttp.responseText;
        }
    }
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


function ismaxlength(obj){
    var mlength=obj.getAttribute? parseInt(obj.getAttribute("maxlength")) : ""
    if (obj.getAttribute && obj.value.length>mlength)
        obj.value=obj.value.substring(0,mlength)
}

function showOverview() {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Overview", true);
    xmlHttp.onreadystatechange = callbackOverview;
    xmlHttp.send();
}

function callbackOverview() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var xmlobject = (new DOMParser()).parseFromString(xmlHttp.responseText, "application/xml");
            var html = xmlobject.getElementsByTagName("htmlSeite");
            document.getElementById("content").innerHTML = html[0].childNodes[0].nodeValue;
        }
    }
}

function showProfile() {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Profile", true);
    xmlHttp.onreadystatechange = callbackProfile;
    xmlHttp.send();
}

function callbackProfile() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var xmlobject = (new DOMParser()).parseFromString(xmlHttp.responseText, "application/xml");
            var html = xmlobject.getElementsByTagName("htmlSeite");
            document.getElementById("content").innerHTML = html[0].childNodes[0].nodeValue;
        }
    }
}

function showTimes() {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Times", true);
    xmlHttp.onreadystatechange = callbackTimes;
    xmlHttp.send();
}

function callbackTimes() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var xmlobject = (new DOMParser()).parseFromString(xmlHttp.responseText, "application/xml");
            var html = xmlobject.getElementsByTagName("htmlSeite");
            document.getElementById("content").innerHTML = html[0].childNodes[0].nodeValue;
        }
    }
}
