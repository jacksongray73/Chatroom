Chatroom Client and Server

Authors:
	Fernandez, Alan
	Gray, Jackson
	Hill, Colton

Overview:
	This project consists of two parts: a client and a server. The server accepts 
	client connections and manages chatrooms while the client connects to the 
	server to chat with other users.

Compilation:
	Both ChatroomClient.java and ChatroomServer.java may be compiled in the 
	terminal with the following commands:

		javac ChatroomClient.java
		javac ChatroomServer.java 

Server Usage:
	The server may be started with the following command:
		java ChatroomServer
	Once started, no further input is needed.

Client Usage:
	The client may be started with the following command:

		java ChatroomClient [IP]

	[IP] is an optional argument, and may be replaced with an IP address or left
	blank. If left blank, the client will default to connect to a server using 
	the address localhost.

	Once the client is running, the user will be prompted to enter a username.
	Afterwards, the user may enter any of the following commands:
		
	- JOIN [name]		Joins a chatroom with the name of the given string. 
				This should be the first command the user enters after
				entering their username.
		
	- LEAVE			Exits the chatroom the user is currently in and exits
				the program.

	- NAME [username]	Changes the user’s name to the given string. This may
				be done at any time.
