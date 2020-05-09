console.log("How did I get here........?");

// Create the web socket
let socket = new WebSocket('ws://localhost:8080');
let checkboxIds = [];
var signInSuccessful;
hideSignOut();

// This is where you handle messages coming in through the web socket.
socket.onmessage = (messageWrapper) => {
    // We go ahead and convert the message into a JSON object.
    let stringMessage = messageWrapper.data;
    let asJSONObject = JSON.parse(stringMessage);

    // Print the object to console for debugging purposes.
    console.log(asJSONObject);

    // This is where we handle different types of messages:

    // Check if the header is for playlists.
    if (asJSONObject.header.localeCompare("playlists") === 0) {
        // Get the playlists div element.
        let workspace = document.getElementById('playlists');
        workspace.innerHTML = "";
        workspace.appendChild(document.createElement('br'));
        // Create an unordered list element.
        // Loops through each playlist in the data array.
        checkboxIds = [];
        asJSONObject.data.forEach((playlist) => {
            // Create a list item element.
            checkboxIds.push(playlist.id);
            let input = document.createElement('input');
            input.type = "checkbox";
            input.id = playlist.id;
            let label = document.createElement('label');
            label.for = playlist.id;
            label.innerText = playlist.snippet.title;
            // Set the list item's text.

            workspace.appendChild(input);
            workspace.appendChild(label);
            workspace.appendChild(document.createElement('br'));
        });

        let sendButton = document.createElement('button');
        sendButton.innerText = 'Click Me!';
        sendButton.onclick = openForm;
        workspace.appendChild(sendButton);
        // Add the unordered list to the playlists div.
    }

    if (asJSONObject.header.localeCompare("successfulSignIn") === 0) {
        signInSuccessful = true;
        showSignOut();
        hideSignIn();

        // Get the playlists div element.
        let header = document.getElementById('email');
        header.innerText = asJSONObject.data;
        // Add the header to the app div.

    }

    if (asJSONObject.header.localeCompare('greeting') === 0) {
        // Get the app div element.
        let workspace = document.getElementById('app');
        // Create a header element.
        let header = document.createElement('h1');
        // Set the header element's text.
        header.innerText = asJSONObject.data;
        // Add the header to the app div.
        workspace.appendChild(header);
    }

    if (asJSONObject.header.localeCompare('successfulSignOut') === 0) {
        showSignIn();
        hideSignOut();
    }

    if (asJSONObject.header.localeCompare('signedIn') === 0) {
        showSignOut();
        hideSignIn();
        let header = document.getElementById('email');
        header.innerText = asJSONObject.data;

    }

    if (asJSONObject.header.localeCompare('signedOut') === 0) {
        showSignIn();
        hideSignOut();
        let header = document.getElementById('email');
        header.innerText = "Not Signed In";
        signInSuccessful = false;
    }
};

// When the web socket connects, send a test message.
// Note: you can only set "socket.onopen" once. Setting it later will override this one.
socket.onopen = () => {
    // Ask for the server's greeting.
    socket.send(`{"header": "getGreeting", "data": ""}`);
    socket.send(`{"header": "isSignedIn", "data": ""}`);
    // You can do other stuff in here too!
};

// Function for requesting my playlists
function getMyPlaylists() {
    // Send the web socket this json formatted message.
    socket.send(`{"header": "getMyPlaylists", "data": ""}`);
}

//function to sign out of your account
function signOut() {
    socket.send(`{"header": "signOut", "data": ""}`);
}

function signIn() {
    socket.send(`{"header": "signIn", "data": ""}`);
    if (signInSuccessful) {
        showSignOut();
        hideSignIn();
    }
}

function hideSignIn() {
    var signInButton = document.getElementById("signInButton");
    signInButton.style.display = "none";
}

function hideSignOut() {
    var signOutButton = document.getElementById("signOutButton");
    signOutButton.style.display = "none";
}

function showSignIn() {
    var signInButton = document.getElementById("signInButton");
    signInButton.style.display = "block";
}

function showSignOut() {
    var signOutButton = document.getElementById("signOutButton");
    signOutButton.style.display = "block";
}

function sendPlaylistIDs(startMillis, expireMillis) {
    checkedIdsToSend = [];
    checkboxIds.forEach((checkboxId) => {
        // Get the checkbox
        let checkbox = document.getElementById(checkboxId);
        if (checkbox.checked === true) {
            checkedIdsToSend.push(checkboxId);
        }
    });
    let message = `{"header": "scheduleLists", "data": {"start": ${startMillis}, "expire": ${expireMillis}, "ids": "[${checkedIdsToSend}]"}}`;
    console.log(message);
    socket.send(message);
}

function openForm() {
    let form = document.createElement('form');

    let startLabel = document.createElement('label');
    startLabel.for = 'start';
    startLabel.innerText = "Start Time";

    let start = document.createElement('input');
    start.type = 'date';
    start.id = 'start';

    let startTime = document.createElement('input');
    startTime.type = 'time';

    form.appendChild(startLabel);
    form.appendChild(start);
    form.appendChild(startTime);

    form.appendChild(document.createElement('br'));

    let expire = document.createElement('input');
    expire.type = 'date';
    expire.id = 'start';

    let expireLabel = document.createElement('label');
    expireLabel.for = 'expire';
    expireLabel.innerText = "Expire Time";

    let expireTime = document.createElement('input');
    expireTime.type = 'time';

    form.appendChild(expireLabel);
    form.appendChild(expire);
    form.appendChild(expireTime)

    form.appendChild(document.createElement('br'));

    let button = document.createElement('input');
    button.type = 'submit';
    button.value = 'Submit';
    form.onsubmit = () => {
        let startMillis = start.valueAsNumber + startTime.valueAsNumber;
        let expireMillis = expire.valueAsNumber + expireTime.valueAsNumber;
        sendPlaylistIDs(startMillis, expireMillis);
    };
    form.appendChild(button);

    let newWindow = window.open('', null, 'width=400,height=100,resizeable,scrollbars');
    newWindow.document.body.appendChild(form);
    button.onclick = () => {
        newWindow.close();
    };
}


