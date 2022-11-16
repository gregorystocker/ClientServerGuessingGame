>Gregory Stocker - Assignment 3 README.md
</br>
   :----------------------------------------------------------------------|:
> VIDEO URL:  https://youtu.be/yYtH6OjDNS0
- a) Detailed Project description (how its structured, what it does)
- My UDP program is a very simple system. The server loads up, and the Client connects to it using a Datagram Socket.
- The Client sends a message to the Server to begin the conversation and in reply the Server asks for the player's name.
- Along with it the server sends over hi.png which displays the work hi. The Client can type in their name and the server
- will send back a greeting. That is all the functionality in the UDP program. 
  - b) 
  Requirements Checklist: (Check if completed)
  
    - [X] 1 Client connects to server upon startup. Server replies by asking
      for players name
    - [X] 2 Client can send name and server will receive it and greet the client by name
    - [X]  3 Client is presented a choice to either see a leader board or play the game
    - [X]  4 Leaderboard shows all players that have played since the server started w/ their name and points. Server maintains the leader board and sends it to teh client if requested. Extra points for leader board saying persistend upon server restart. (Havent covered this yet)
    - [X] 5 If client wants to start, server sends over a first quote of a character. Print the intended answer in the server terminal
    - [X] 6 Client can enter a guess, "more" or "next
    - [X] 7 If guess- server checks guess and responds. If answer is correct, client gets a new pic w/ new quote (or win message.) If wrong, tell player it was wrong and let them try again. 
    - [X] 8 If more- client gets a new quote, but from the same character. If there's no more new pictures, tell the player that. 
    - [X] 9 ~~If next- server sends the client a new quite for a new character. If no new characters left, show an old one or tell the user and quit. Just don't let it crash.
    - [X] 10 If~~ server gets 3 correct guesses and time isn't over, server sends "winner" image and display to client.
    - [X] 11 If server receives guess and the timer ran out, the client list and gets a "loser" image + message.  
    - [X] 12 Implement a point system. Give more points for answering faster w/o asking for more quotes. State management of points is done on the server side. If answer on 1st quote: 5 pts., 2nd: 4 pts., 3rd: 3 pts. last: 1 pt. For each "next" by user, they lose 2 pts. POINTS CAN BE NEGATIVE. Always display current points. 
    - [X] 13 At end, for win or loss, display total points. If lost, don't change the leader board. Assume name is a player name is a unique id. 
    - [X] 14 Evaluate input on the server side. Client should not know pics, answers, points or leader board. Answers are not sent to client. 
    - [X] 15 Make protocol robust. IF a command isn't understood, the protocol should define how to deal with those issues. Protocol headers are mandatory, payloads are optional. Reciever should be able to read the header, understand the metadata, and use to assis with payload processing. Describe the protocol in detail in the README.
    - [X] 16 If errors occur on client or server or network issues crops up, consider how these are handled in a recoverable and informative way. Use good error handling and output. Dont let the client/server crash upon invalid input. 
    - [ ] 17 After a win/loss, let user start a new game by reentering their name, or let them quit by typing quit. After reenter, allow to see play, or leaderboard option again. 
    - [ ] 18 (skip this one until all else is done) If a client tries to connect while another client is already connected, tell themn there is a game in progress and they cant connect. NO THREADS ALLOWED.
  </br>
  
    - c) An explanation of how we can run the program:
      go into the udp folder
      For defaults of localhost and port 8080:
    Server: gradle runServer
    Client: gradle runClient
      To specify the ports/host:
    Server: gradle runServer −Pport=8080
    Client: gradle runClient −Pport=8080 −Phost=localhost

- d) UML diagram showing communication between client and server:
- see UDPDiagram.png

- e)Protocol description. (For each request/possible responses):
    My request JSON looks like this:
    
  {
      "header":
      {
          "datatype":     "null",
          "type":         "null"
      },
      "payload":
      {
          "data":          "null",
          }
}

- My response JSON looks like this:
  {
      "header":
      {
          "datatype":       "null",
          "type":           "null"
      },
      "payload":
      {
          "data":          "null",
          "picture" :      "null",
          "points" :       "null",
          "leaderboard" :  "null"

          }
}
How my protocol works: 
  The type attribute in the header relays information how to handle the payload for both requests and responses.
  In the Clients request it has a few different variations:
  
  begin: Tells the server that Client is ready. Prompts server to ask for a name. 
  getName: Tells the server the payload's "data" should be interpreted as the client's name unless they enter "leaderboard", in which case Server will display the leaderboard and interpret
  
 
 The servers response uses a simmilar method:
 begin: Tells the client the data in the payload is a greeting, and sends over the hi.png picture in the payloads "picture" attribute that needs 
 to be decode from the Base64 string back to an image and display.
 getName: Tells the Client that the data is an acknowledgement of their name to be displayed. Sends over the first characters picture in the payloads "picture" attribute that needs 
 to be decode from the Base64 string back to an image and display. 

- f) How my program is robust: My program is able to handle all types of user input as it interprets as a string. Likewise,
- any image that is too large for a single DataGramPacket will be split in to multiple and still successfully be sent over.
-  video: https://youtu.be/yYtH6OjDNS0

 </br>
 :---------------------------------------------------------------------|:
