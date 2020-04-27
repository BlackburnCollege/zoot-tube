console.log("How did I get here........?")

// Create the web socket
let socket = new WebSocket('ws://localhost:8080');

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
        // Create an unordered list element.
        let list = document.createElement('ul');
        // Loops through each playlist in the data array.
        asJSONObject.data.forEach((playlist) => {
            // Create a list item element.
            let item = document.createElement('li');
            // Set the list item's text.
            item.innerText = playlist.snippet.title;
            // Add the list item to the unordered list.
            list.appendChild(item);
        });
        // Add the unordered list to the playlists div.
        workspace.appendChild(list);
    }

    // Check if the header is for a greeting.
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
};

// When the web socket connects, send a test message.
// Note: you can only set "socket.onopen" once. Setting it later will override this one.
socket.onopen = () => {
    // Ask for the server's greeting.
    socket.send(`{"header": "getGreeting", "data": ""}`);
    // You can do other stuff in here too!
};

// Function for requesting my playlists
function getMyPlaylists() {
    // Send the web socket this json formatted message.
    socket.send(`{"header": "getMyPlaylists", "data": ""}`);
}

//function to sign out of your account
function signOut(){
    socket.send(`{"header": "signOut", "data": ""}`);
}

function signIn(){
    socket.send(`{"header": "signIn", "data": ""}`);
}