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
    document.getElementById("selected_tab").style.marginLeft = "134px";
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

function removeMeFromModule(id) {
    document.getElementById("statusBox").innerHTML = "Bitte warten ...";
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Modules?removeFromModule="+id, true);
    xmlHttp.onreadystatechange = callbackModules;
    xmlHttp.send();
}

function saveMessage(id) {
    document.getElementById("statusBox").innerHTML = "Bitte warten ...";
    var message = document.getElementById("messageArea").value;
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Modules?saveMessage="+message+"&id="+id, true);
    xmlHttp.onreadystatechange = callbackShowModuleDescription;
    xmlHttp.send();
}

function callbackModules() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var xmlobject = (new DOMParser()).parseFromString(xmlHttp.responseText, "application/xml");
            var html = xmlobject.getElementsByTagName("htmlSeite");
            var modulesCount = xmlobject.getElementsByTagName("modulesCount")[0].childNodes[0].nodeValue;
            var error = xmlobject.getElementsByTagName("error")[0].childNodes[0].nodeValue;
            var errorMsg = xmlobject.getElementsByTagName("errorMsg")[0].childNodes[0].nodeValue;
            if (error > 0) {
                document.getElementById("statusBox").innerHTML = errorMsg;
            } else {
                if (error == -1) {
                    document.getElementById("statusBox").innerHTML = errorMsg;
                }
                if (modulesCount > 10) {
                //    document.getElementById("content").style.height = modulesCount*40 + "px";
                }
                document.getElementById("content").innerHTML = html[0].childNodes[0].nodeValue;
            }
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            }
        }
    }
}

function showMembers() {
    document.getElementById("selected_tab").style.marginLeft = "545px";
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Members", true);
    xmlHttp.onreadystatechange = callbackMembers;
    xmlHttp.send();
}

function addUser() {
    var html = "<html><head><script src=\"jsXMLHttpRequestHandle.js\" type=\"text/javascript\"></script></head><body><table border=\"0\">"
    + "<tr>"
    + "<td>Name:<input type=\"text\" id=\"name\" maxlength=\"40\"/></td>"
    + "</tr>"
    + "<tr>"
    + "<td>Vorname:<input type=\"text\" id=\"firstname\" maxlength=\"40\"/></td>"
    + "</tr>"
    + "<tr>"
    + "<td>Email: <input type=\"text\" onkeyup=\"if (event.keyCode != 37 && event.keyCode != 39 ) {printWait('statusEmail');validateEmailServletMembers()}\" id=\"email\"/ maxlength=\"40\"></td><td><div id=\"imgEmail\"></div></td><td><div id=\"statusEmail\"></div></td>"
    + "</tr>"
    + "<tr>"
    + "<td><input id=\"submitData\" type=\"button\" value=\"Speichern\" onclick=\"saveUser()\"/></td><td><input type=\"button\" value=\"Abbrechen\" onclick=\"hideAddUser()\"/></td>"
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
            if (membersInModuleBox.innerHTML.charAt(i) == "[") {
                var tmpMember = "";
                i++;
                while (membersInModuleBox.innerHTML.charAt(i) != "]") {
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
        if (membersInModuleBox.innerHTML.charAt(i) == "[") {
            var fromPos = i;
            var tmpMember = "";
            i++;
            while (membersInModuleBox.innerHTML.charAt(i) != "]") {
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
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            }
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

    if (name.length != 0 
        && description.length != 0
        && startDay.length != 0
        && startMonth.length != 0
        && startYear.length != 0
        && endDay.length != 0
        && endMonth.length != 0
        && endYear.length != 0) {
        if (parseInt(startDay)==startDay-0
            && parseInt(startMonth)==startMonth-0   //zahlenwerte ?
            && parseInt(startYear)==startYear-0
            && parseInt(endDay)==endDay-0
            && parseInt(endMonth)==endMonth-0
            && parseInt(endYear)==endYear-0) {


            if (startYear > endYear || startYear < 0 || endYear < 0 || startYear.length != 2 && startYear.length != 4 || endYear.length != 2 && endYear.length != 4) {
                if (startYear < 0 || endYear < 0) {
                    document.getElementById("statusBox").innerHTML = "Ihr Termin ist nicht zuelaessig (Jahr)";
                } else {
                    document.getElementById("statusBox").innerHTML = "Ihr Starttermin ist spaeter als der Endtermin! (Jahr)";
                }
                if (startYear.length != 2 && startYear.length != 4 || endYear.length != 2 && endYear.length != 4) {
                    document.getElementById("statusBox").innerHTML = "Das Jahr ist vom Format XX oder XXXX (Jahr)";
                }
                return;
            } else {
                if (startYear == endYear) {
                    if (startMonth > endMonth || startMonth > 12 || startMonth < 1 || endMonth > 12 || endMonth < 1) {
                        if (startMonth > 12 || startMonth < 1 || endMonth > 12 || endMonth < 1) {
                            document.getElementById("statusBox").innerHTML = "Ihr Termin ist nicht zuelaessig (Monat)";
                        } else {
                            document.getElementById("statusBox").innerHTML = "Ihr Starttermin ist spaeter als der Endtermin! (Monat)";
                        }
                        return;
                    } else {
                        if (startMonth == endMonth) {
                            if (startDay > endDay || startDay > 31 || startDay < 1 || endDay > 31 || endDay < 1) {
                                if (startDay > 31 || startDay < 1 || endDay > 31 || endDay < 1) {
                                    document.getElementById("statusBox").innerHTML = "Ihr Termin ist nicht zuelaessig (Tag)";
                                } else {
                                    document.getElementById("statusBox").innerHTML = "Ihr Starttermin ist spaeter als der Endtermin! (Tag)";
                                }
                                return;
                            }
                        }
                    }

                }
            }
            var query = "description=" + description + "&" +
            "name=" + name + "&" +
            "startDate=" + (startYear.length == 2 ? "20" + startYear : startYear) + "-" + (startMonth.length == 1 ? "0" + startMonth : startMonth) + "-" + (startDay.length == 1 ? "0" + startDay : startDay) + "&" +
            "endDate=" + (endYear.length == 2 ? "20" + endYear : endYear) + "-" + (endMonth.length == 1 ? "0" + endMonth : endMonth) + "-" + (endDay.length == 1 ? "0" + endDay : endDay) + "&" +
            "membersToAdd=" + membersToAdd + "&" +
            "prio=" + prio;

            xmlHttp.open('POST',"/Projektmanager/Modules?" + query, true);
            xmlHttp.onreadystatechange = callbackModules;
            xmlHttp.send();
        } else {
            document.getElementById("statusBox").innerHTML = "Ein Datum darf keine Buchstaben enthalten!";
        }
    } else {
        document.getElementById("statusBox").innerHTML = "Bitte geben Sie in jedes Feld etwas ein!";
    }
}

function saveUser() {
    createXMLHttpRequest();
    var name = document.getElementById("name").value;
    var firstname = document.getElementById("firstname").value;
    var email = document.getElementById("email").value;
    if (name.length != 0 && firstname.length != 0 && email.length != 0) {
        xmlHttp.open('POST',"/Projektmanager/Members?addName=" + name + "&addFirstname=" + firstname + "&addEmail=" + email, true);
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
    document.getElementById("statusBox").innerHTML = "Bitte warten ...";
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
    xmlHttp.onreadystatechange = callbackModules;
    xmlHttp.send();
}



function changeStatus(status, email) {
    document.getElementById("userDescription").innerHTML = "";
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Members?changeStatus=" + status + "&email=" + email, true);
    xmlHttp.onreadystatechange = callbackShowUserDescription;
    xmlHttp.send();
}
function callbackShowModuleDescription() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var xmlobject = xmlHttp.responseXML;
            var html = xmlobject.getElementsByTagName("htmlSeite");
            var error = xmlobject.getElementsByTagName("error")[0].childNodes[0].nodeValue;
            document.getElementById("statusBox").innerHTML = "";
            if (error > 0) {
                document.getElementById("statusBox").innerHTML = html[0].childNodes[0].nodeValue;
            } else {
                document.getElementById("addModule").style.border = "0px";
                document.getElementById("addModule").innerHTML = html[0].childNodes[0].nodeValue;
            }
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            }
        }
    }
}

function deleteMessage(modulid, messageid) {
    createXMLHttpRequest();
    xmlHttp.open('POST',"/Projektmanager/Modules?deleteMessageId=" + messageid + "&modulid=" + modulid, true);
    xmlHttp.onreadystatechange = callbackShowModuleDescription;
    xmlHttp.send();
}

function callbackShowUserDescription() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var xmlobject = xmlHttp.responseXML;
            var html = xmlobject.getElementsByTagName("htmlSeite");
            var message = xmlobject.getElementsByTagName("message")[0].childNodes[0].nodeValue;
            if (message != " ") {
                document.getElementById("statusBox").innerHTML = message;
            }
            document.getElementById("userDescription").innerHTML = html[0].childNodes[0].nodeValue;
            
            
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            }
        }
    }
}

function hideAddUser() {
    document.getElementById("addUserField").innerHTML = "";
}

function validateProjectServlet()
{
    if (document.getElementById("projectname").value != "") {
        var servlet = "/Projektmanager/ValidateProjectServlet";
        servlet += "?projectname=" + document.getElementById("projectname").value;
        createXMLHttpRequest();
        xmlHttp.open('POST',servlet, true);
        xmlHttp.onreadystatechange = callbackValidateProject;
        xmlHttp.send(null);
    } else {
        document.getElementById("imgProjectname").innerHTML = "<img src=grafik/error.gif />";
        document.getElementById("statusProjectname").innerHTML = "Bitte Projektname eingeben!";
    }
    
}

function callbackValidateProject() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var status = xmlHttp.responseText;
            if (status == "0") {
                document.getElementById("imgProjectname").innerHTML = "<img src=grafik/ok.gif />";
                document.getElementById("statusProjectname").innerHTML = "Projektname noch nicht vergeben!";
            } else {
                document.getElementById("imgProjectname").innerHTML = "<img src=grafik/error.gif />";
                document.getElementById("statusProjectname").innerHTML = "Projektname schon vergeben!";
            }
            checkInputs();
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            } else {
                document.getElementById("statusProjectname").innerHTML = "Fehler bei der Valiedierung des Projektnamens";
            }
        }
    }
}

function validateEmailServlet()
{
    if (document.getElementById("email").value != "") {
        var email = document.getElementById("email").value;
        var emailRegxp =/^.+@.+\..{2,5}$/;
        if(!email.match(emailRegxp)) {
            document.getElementById("imgEmail").innerHTML = "<img src=grafik/error.gif />";
            document.getElementById("statusEmail").innerHTML = "Keine regul&auml;re E-Mail eingegeben!";
            checkInputs();
            return;
        }

        var servlet = "/Projektmanager/ValidateEmailServlet";
        servlet += "?email=" + email;
        createXMLHttpRequest();
        xmlHttp.open('POST',servlet, true);
        xmlHttp.onreadystatechange = callbackValidateEmail;
        xmlHttp.send(null);
    } else {
        document.getElementById("imgEmail").innerHTML = "<img src=grafik/error.gif />";
        document.getElementById("statusEmail").innerHTML = "Bitte E-Mail eingeben!"
    }
}

function validateEmailServletMembers()
{
    if (document.getElementById("email").value != "") {
        var email = document.getElementById("email").value;
        var emailRegxp =/^.+@.+\..{2,5}$/;
        if(!email.match(emailRegxp)) {
            document.getElementById("imgEmail").innerHTML = "<img src=grafik/error.gif />";
            document.getElementById("statusEmail").innerHTML = "Keine regul&auml;re E-Mail eingegeben!";
            checkInputsMembers();
            return;
        }

        var servlet = "/Projektmanager/ValidateEmailServlet";
        servlet += "?email=" + email;
        createXMLHttpRequest();
        xmlHttp.open('POST',servlet, true);
        xmlHttp.onreadystatechange = callbackValidateEmailMembers;
        xmlHttp.send(null);
    } else {
        document.getElementById("imgEmail").innerHTML = "<img src=grafik/error.gif />";
        document.getElementById("statusEmail").innerHTML = "Bitte E-Mail eingeben!"
    }
}

function callbackValidateEmail() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var status = xmlHttp.responseText;
            if (status == "0") {
                document.getElementById("imgEmail").innerHTML = "<img src=grafik/ok.gif />";
                document.getElementById("statusEmail").innerHTML = "E-Mail noch nicht vergeben!";
            } else {
                document.getElementById("imgEmail").innerHTML = "<img src=grafik/error.gif />";
                document.getElementById("statusEmail").innerHTML = "E-Mail schon vergeben!";
            }
            checkInputs();
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            } else {
                document.getElementById("statusEmail").innerHTML = "Fehler bei der Valiedierung der E-Mail Adresse";
            }
        }
    }
}
function callbackValidateEmailMembers() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var status = xmlHttp.responseText;
            if (status == "0") {
                document.getElementById("imgEmail").innerHTML = "<img src=grafik/ok.gif />";
                document.getElementById("statusEmail").innerHTML = "E-Mail noch nicht vergeben!";
            } else {
                document.getElementById("imgEmail").innerHTML = "<img src=grafik/error.gif />";
                document.getElementById("statusEmail").innerHTML = "E-Mail schon vergeben!";
            }
            checkInputsMembers();
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            } else {
                document.getElementById("statusEmail").innerHTML = "Fehler bei der Valiedierung der E-Mail Adresse";
            }
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
                document.write("<a href=\"Login.html\">Registrierung erfolgreich! Ihnen wurde eine E-Mail zugestellt mit ihren Zugangsdaten.</a>");
            else
                document.getElementById("statusSubmit").innerHTML = "Es gab einen Fehler";
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            }
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
            //    document.getElementById("content").style.height = membersCount*40 + "px";
            }
            document.getElementById("content").innerHTML = html[0].childNodes[0].nodeValue;
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            }
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
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            } else {
                document.write("<a href=\"Login.html\">Schwerwiegender Fehler.</a>");
            }
        }
    }
}


function ismaxlength(obj){
    var mlength=obj.getAttribute? parseInt(obj.getAttribute("maxlength")) : ""
    if (obj.getAttribute && obj.value.length>mlength)
        obj.value=obj.value.substring(0,mlength)
}

function showOverview() {
    document.getElementById("selected_tab").style.marginLeft = "0px";
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
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            }
        }
    }
}

function showProfile() {
    document.getElementById("selected_tab").style.marginLeft = "407px";
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
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            }
        }
    }
}

function showTimes() {
    document.getElementById("selected_tab").style.marginLeft = "270px";
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
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            }
        }
    }
}

var a = 0;
function showHint(element, text) {
    if (a == 0) {
        a = 1;
        element.innerHTML = "<div style=\"position:absolute; background-image:url(grafik/hint.png); margin-left:70px; margin-top:-30px; width:145px; height:57px;\" align=\"center\"><font size=1>" + text + "</font></div>";
    } else {
        a = 0;
        element.innerHTML = "";
    }
}

function changePassword() {
    document.getElementById("statusBox").innerHTML = "Bitte warten...";
    var oldPassword = document.getElementById("oldPassword").value;
    var newPassword = document.getElementById("newPassword").value;
    var validatePassword = document.getElementById("validatePassword").value;
    createXMLHttpRequest();
    var servlet = "/Projektmanager/ChangePassword?oldPassword=" + oldPassword + "&newPassword=" + newPassword + "&validatePassword=" + validatePassword;
    xmlHttp.open('POST',servlet, true);
    xmlHttp.onreadystatechange = callbackPassword;
    xmlHttp.send();
}

function callbackPassword() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            document.getElementById("statusBox").innerHTML = xmlHttp.responseText;
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            }
        }
    }
}

function pressedMenueButton(element) {
    element.style.backgroundImage = "url:(grafik/button_pressed.png)";
}

function saveTimes() {
    document.getElementById("statusBox").innerHTML = "Bitte warten...";
    var modul = document.getElementById("modul").value;
    var date = document.getElementById("date").value;
    var start = document.getElementById("start").value;
    var end = document.getElementById("end").value;
    var description = document.getElementById("description").value;
    
    var servlet = "/Projektmanager/Times?modul=" + modul + "&date=" + date + "&start=" + start + "&end=" + end + "&description=" + description;
    xmlHttp.open('POST',servlet, true);
    xmlHttp.onreadystatechange = callbackSaveTimes;
    xmlHttp.send();
}

function callbackSaveTimes() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var xmlobject = (new DOMParser()).parseFromString(xmlHttp.responseText, "application/xml");
            var html = xmlobject.getElementsByTagName("htmlSeite");
            document.getElementById("content").innerHTML = html[0].childNodes[0].nodeValue;
            
            var status = xmlobject.getElementsByTagName("status")[0].childNodes[0].nodeValue;
            document.getElementById("statusBox").innerHTML = status;

            if (xmlobject.getElementsByTagName("modul")[0].childNodes[0].nodeValue != "") {
                document.getElementById("date").value = xmlobject.getElementsByTagName("date")[0].childNodes[0].nodeValue;
                document.getElementById("start").value = xmlobject.getElementsByTagName("start")[0].childNodes[0].nodeValue;
                document.getElementById("end").value = xmlobject.getElementsByTagName("end")[0].childNodes[0].nodeValue;
                document.getElementById("description").value = xmlobject.getElementsByTagName("description")[0].childNodes[0].nodeValue;
            }
        }
    }
}

function now(id) {
    var time = new Date();
    document.getElementById(id).value = time.getHours() + ":" + time.getMinutes();
}

function today(id) {
    var date = new Date();
    document.getElementById(id).value = date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
}

function checkDate() {
    var index;

    var startDay;
    var startMonth;
    var startYear = document.getElementById("startProject").value;
    if (startYear != "") {
        index = startYear.indexOf(".");
        if (index != -1) {
            startDay = startYear.substring(0, index);
            if (startDay.length != 2) {
                document.getElementById("statusDate").innerHTML = "Falsches Format beim Startdatum! Bitte folgendes Format verwenden: dd.mm.yyyy";
                return;
            }
            startYear = startYear.substring(index + 1);

            index = startYear.indexOf(".")
            if (index != -1) {
                startMonth = startYear.substring(0, index);
                if (startMonth.length != 2) {
                    document.getElementById("statusDate").innerHTML = "Falsches Format beim Startdatum! Bitte folgendes Format verwenden: dd.mm.yyyy";
                    return;
                }
                startYear = startYear.substring(index + 1);
                if (startYear.length != 4) {
                    document.getElementById("statusDate").innerHTML = "Falsches Format beim Startdatum! Bitte folgendes Format verwenden: dd.mm.yyyy";
                    return;
                }
            } else {
                document.getElementById("statusDate").innerHTML = "Falsches Format beim Startdatum! Bitte folgendes Format verwenden: dd.mm.yyyy";
                return;
            }
        } else {
            document.getElementById("statusDate").innerHTML = "Falsches Format beim Startdatum! Bitte folgendes Format verwenden: dd.mm.yyyy";
            return;
        }
    }

    
    var endDay;
    var endMonth;
    var endYear = document.getElementById("endProject").value;
    if (endYear != "") {
        index = endYear.indexOf(".");
        if (index != -1) {
            endDay = endYear.substring(0, index);
            if (endDay.length != 2) {
                document.getElementById("statusDate").innerHTML = "Falsches Format beim Enddatum! Bitte folgendes Format verwenden: dd.mm.yyyy";
                return;
            }
            endYear = endYear.substring(index + 1);

            index = endYear.indexOf(".")
            if (index != -1) {
                endMonth = endYear.substring(0, index);
                if (endMonth.length != 2) {
                    document.getElementById("statusDate").innerHTML = "Falsches Format beim Enddatum! Bitte folgendes Format verwenden: dd.mm.yyyy";
                    return;
                }
                endYear = endYear.substring(index + 1);
                if (endYear.length != 4) {
                    document.getElementById("statusDate").innerHTML = "Falsches Format beim Enddatum! Bitte folgendes Format verwenden: dd.mm.yyyy";
                    return;
                }
            } else {
                document.getElementById("statusDate").innerHTML = "Falsches Format beim Enddatum! Bitte folgendes Format verwenden: dd.mm.yyyy";
                return;
            }
        } else {
            document.getElementById("statusDate").innerHTML = "Falsches Format beim Enddatum! Bitte folgendes Format verwenden: dd.mm.yyyy";
            return;
        }
    }

    if (startYear != "") {
        if (startDay < 1 || startDay > 31) {
            document.getElementById("statusDate").innerHTML = "Ungueltiger Tag beim Startdatum!";
            return;
        }

        if (startMonth < 1 || startMonth > 12) {
            document.getElementById("statusDate").innerHTML = "Ungueltiger Monat beim Startdatum!";
            return;
        }

        if (startYear < 2010 || startYear > 9999) {
            document.getElementById("statusDate").innerHTML = "Ungueltiges Jahr beim Startdatum!";
            return;
        }

    }
    
    if (endYear != "") {
        if (endDay < 1 || endDay > 31) {
            document.getElementById("statusDate").innerHTML = "Ungueltiger Tag beim Enddatum!";
            return;
        }
        if (endMonth < 1 || endMonth > 12) {
            document.getElementById("statusDate").innerHTML = "Ungueltiger Monat beim Enddatum!";
            return;
        }
        if (endYear < 2010 || endYear > 9999) {
            document.getElementById("statusDate").innerHTML = "Ungueltiges Jahr beim Enddatum!";
            return;
        }
    }

    if (startYear != "" && endYear != "") {
        if (endYear < startYear) {
            document.getElementById("statusDate").innerHTML = "Enddatum muss nach Startdatum liegen!";
            return;
        } else if (endYear == startYear) {
            if (endMonth < startMonth) {
                document.getElementById("statusDate").innerHTML = "Enddatum muss nach Startdatum liegen!";
                return;
            } else if (endMonth == startMonth) {
                if (endDay <= startDay) {
                    document.getElementById("statusDate").innerHTML = "Enddatum muss nach Startdatum liegen!";
                    return;
                } else {
                    document.getElementById("statusDate").innerHTML = "Datum OK!";
                    checkInputs();
                }
            } else {
                document.getElementById("statusDate").innerHTML = "Datum OK!";
                checkInputs();
            }
        } else {
            document.getElementById("statusDate").innerHTML = "Datum OK!";
            checkInputs();
        }
    }
}

function checkInputs() {
    if (document.getElementById("statusEmail").innerHTML == "E-Mail noch nicht vergeben!" &&
            document.getElementById("statusDate").innerHTML == "Datum OK!" &&
            document.getElementById("statusProjectname").innerHTML == "Projektname noch nicht vergeben!") {
        document.getElementById("submitData").disabled = false;
    } else {
        document.getElementById("submitData").disabled = true;
    }
}

function checkInputsMembers() {
    if (document.getElementById("statusEmail").innerHTML == "E-Mail noch nicht vergeben!") {
        document.getElementById("submitData").disabled = false;
    } else {
        document.getElementById("submitData").disabled = true;
    }
}


function deleteTime(user_id, date, start) {
    document.getElementById("statusBox").innerHTML = "Bitte warten...";

    var servlet = "/Projektmanager/Times?user_id=" + user_id + "&date=" + date + "&start=" + start;
    xmlHttp.open('POST',servlet, true);
    xmlHttp.onreadystatechange = callbackDeleteTimes;
    xmlHttp.send();
}

function callbackDeleteTimes() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var xmlobject = (new DOMParser()).parseFromString(xmlHttp.responseText, "application/xml");
            var html = xmlobject.getElementsByTagName("htmlSeite");
            document.getElementById("content").innerHTML = html[0].childNodes[0].nodeValue;

            var status = xmlobject.getElementsByTagName("status")[0].childNodes[0].nodeValue;
            document.getElementById("statusBox").innerHTML = status;
        }
    }
}


function validateEmailServletPasswordForget()
{
    document.getElementById("submitPasswordForget").disabled = true;
    document.getElementById("statusEmail").disabled = "Bitte warten ...";
    if (document.getElementById("email").value != "") {
        var email = document.getElementById("email").value;
        var emailRegxp =/^.+@.+\..{2,5}$/;
        if(!email.match(emailRegxp)) {
            document.getElementById("imgEmail").innerHTML = "<img src=grafik/error.gif />";
            document.getElementById("statusEmail").innerHTML = "Keine regul&auml;re E-Mail eingegeben!";
            checkInputs();
            return;
        }

        var servlet = "/Projektmanager/ValidateEmailServlet?email=" + email;
        createXMLHttpRequest();
        xmlHttp.open('POST',servlet, true);
        xmlHttp.onreadystatechange = callbackValidateEmailPasswordForget;
        xmlHttp.send(null);
    } else {
        document.getElementById("imgEmail").innerHTML = "<img src=grafik/error.gif />";
        document.getElementById("statusEmail").innerHTML = "Bitte E-Mail eingeben!"
    }
}

function callbackValidateEmailPasswordForget() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var status = xmlHttp.responseText;
            if (status == "0") {
                document.getElementById("imgEmail").innerHTML = "<img src=grafik/error.gif />";
                document.getElementById("statusEmail").innerHTML = "E-Mail nicht vorhanden!";
                document.getElementById("submitPasswordForget").disabled = true;
            } else {
                document.getElementById("imgEmail").innerHTML = "<img src=grafik/ok.gif />";
                document.getElementById("statusEmail").innerHTML = "E-Mail ok!";
                document.getElementById("submitPasswordForget").disabled = false;
            }
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            } else {
                document.getElementById("statusEmail").innerHTML = "Fehler bei der Valiedierung der E-Mail Adresse";
            }
        }
    }
}

function passwordForget(servlet) {
    var email = document.getElementById("email").value;
    servlet += "?email=" + email;
    createXMLHttpRequest();
    xmlHttp.open('POST',servlet, true);
    xmlHttp.onreadystatechange = callbackPasswordForget;
    xmlHttp.send(null);
}

function callbackPasswordForget() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            document.write(xmlHttp.responseText);
        } else {
            if (xmlHttp.status == 401) {    //nicht authorisiert
                window.location.href = "Login.html";
            } else {
                document.write("<a href=\"Login.html\">Schwerwiegender Fehler.</a>");
            }
        }
    }
}

function deleteAccount() {
    bestaetigt = window.confirm ("Wollen Sie wirklich Ihren Account loeeschen?");
    if (bestaetigt == true) {
        createXMLHttpRequest();
        xmlHttp.open('POST',"/Projektmanager/DeleteAccount", true);
        xmlHttp.onreadystatechange = callbackDeleteAccoutn;
        xmlHttp.send(null);
    }   
}

function callbackDeleteAccoutn() {
    if (xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
            var xmlobject = xmlHttp.responseXML;
            var html = xmlobject.getElementsByTagName("htmlSeite");
            var message = xmlobject.getElementsByTagName("message")[0].childNodes[0].nodeValue;
            if (message != " ") {
                document.getElementById("statusBox").innerHTML = message;
            } else {
                document.write(html[0].childNodes[0].nodeValue);
            }
        } else {
            document.getElementById("statusBox").innerHTML = "Fehler";
        }
    }
}