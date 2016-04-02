# TCPchat
Basic Client/Server TCP communication using Android.

Start app,
it will open a form to insert your data:
  name: the name you want to use in the chat
  clientIP: the ip of the server you want to connect to
  clientPort: the port of the server you want to connect to
  serverPort: the port of the server you want to create

if you press the button "Testing Off" the data will be set as follows:
  name: Agilulfo
  clientIP: your device ip
  clientPort: 6789
  serverPort: 6789
in this way you will chat with yourself as client and ALSO as server


When inserted consistent data and pressed the "Ok" button you have to:
-start a server: so a client can connect to you (at the port you specified before)
-start a client: connecting to a server with IP and Port you inserted before

when the server, or the client, or both are started you should be able to send messages to:
-a client connected to you (if you started your server)
-a server to which you have connected (if you started your client)

At this point if you start the client without a server listening there will be problems.
If something doesn't work, restart the app and hope!
