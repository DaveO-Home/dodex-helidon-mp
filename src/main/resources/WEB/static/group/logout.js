function logout(secUrl, redirUrl) {
    if (bowser.msie) {
        document.execCommand('ClearAuthenticationCache', 'false');
    } else if (bowser.gecko) {
        $.ajax({
            async: false,
            url: secUrl,
            type: 'GET',
            username: 'logout'
        });
    } else if (bowser.webkit || bowser.chrome) {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.open(\"GET\", secUrl, true);
        xmlhttp.setRequestHeader(\"Authorization\", \"Basic logout\");
        xmlhttp.send();
    } else {
// http://stackoverflow.com/questions/5957822/how-to-clear-basic-authentication-details-in-chrome
        redirUrl = url.replace('http://', 'http://' + new Date().getTime() + '@');
    }
    setTimeout(function () {
        window.location.href = redirUrl;
    }, 200);
}