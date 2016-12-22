// toggle between the connect button and the qr code
function toggle() {
    var html = document.getElementsByTagName("html")[0];
    html.classList.toggle('no-desktop');
}


function deviceCheck() {
    var html = document.getElementsByTagName("html")[0];
    var ua = navigator.userAgent;
    var checker = {
        ios: ua.match(/(iPhone|iPod|iPad)/),
        android: ua.match(/Android/)
    };

    if (checker.android){
        html.classList.add('no-desktop');
        html.classList.add('android');
    }
    else if (checker.ios){
        html.classList.add('no-desktop');
        html.classList.add('ios');
    }
    // else {
    //     html.classList.add('desktop');
    // }
}


window.onload = function() {
    deviceCheck();
};




var watchUrl = "$notificationuri$";
console.log("watching ", watchUrl);
var evtSource = new EventSource(watchUrl);

evtSource.addEventListener("loggedIn", function(e) {
    console.log("loggedIn received", e);
    data = JSON.parse(e.data);
    if (data.page) {
        var a = new DOMParser().parseFromString(data.page, "text/html");
        var b = document.importNode(a.querySelector("form"), true);
        document.getElementById("formHolder").appendChild(b);
        document.forms[0].submit();
    } else if (data.url) {
        document.location.href = data.url;
    } else {
        console.log("do not know what to do with data");
    }
}, false);

evtSource.onerror = function(e) {
    console.log("error, closing", arguments);
    evtSource.close();
    evtSource = null;
};

