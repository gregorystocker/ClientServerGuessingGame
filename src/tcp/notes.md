- [X] Create udp and tcp folders
- [X] give each a build.gradle
- [X] Flesh out Client build.gradle
- [] Flesh out Server build.gradle
- [] bulid UML diagram depicting communication (sequence diagram?)
- [X] Set default port to 8080 and default host to localhost
- [] Create a video 4-7 mins. showing me running the program and showing features. (Use AWS not just localhost)
- [] If using networking code from AdvancedCustomProtocol, add 2-4 mins. and explain the networking part and how messages are sent out. 
- [] watch SI video going over the UI structure





SI Advice: 
 
Use the same grid coordinate that is shown in the template readMe.

DONT NEED TO USE:
GridMaker Class: 
	You dont need GridMaker at all, its left over from a previous version of the assignment.
	main.insertImage("somePicture.jpg",0,1); is all you need.


How to use: 
PicturePanel Class:
	 uewGame(int dimension); - creates a new game & set grid size to dimension x dimension
	 	doesnt do much except display one picture. 
	 	Good starting point!
	 	Flesh this out. 
	 
	 insertImage(String fname, int row, int col); Insert image at col,row
	 
OutputPanel Class:

	MOST IMPORTANT METHODS:
	getInputText(): get the input text box text
	appendOutput(String message) : Add message to output text
	
	OTHER USEFUL METHODS:
	setInputText(String newText) : set the input text box text
	addEventHandlers(EventHandlers handerObj) Add evet listeners
	
	Walkthrough:
	
Image is displayed with a quote and you have to guess who said that quote	


