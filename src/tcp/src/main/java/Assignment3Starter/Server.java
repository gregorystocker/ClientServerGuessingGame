package Assignment3Starter;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.time.*;


import org.json.*;

public class Server {
    //server member variables:
    //maps the character's name to an array of it's corresponding image
    //names
    static String[] names = {"captain america","darth vader","homer simpson",
            "jack sparrow","joker","tony stark","wolverine"};
    static String[] directoryNames = {"Captain_America","Darth_Vader","Homer_Simpson",
            "Jack_Sparrow","Joker","Tony_Stark","Wolverine"};
    public static HashMap<String, CharacterGroup> imageMap;
    public static CharacterGroup currentCharacter;
    public static int morePtr = 0;
    public static int numGuesses = 0;
    public static LocalTime start;
    enum GameState {
        GUESSING,
        WIN,
        LOSE,
        GETNAME,
        MORE,
        BEGIN,
        NEXT,
        LEADERBOARD
    }
    static GameState gameState = GameState.BEGIN;

    //keeps track of how many correct the user has gotten
    //3 within the time frame = a win
    static int correctGuesses=0;
    //For each CharacterGroup: 1st quote = 5 pts, 2nd = 4 pts, 3rd = 3pts, 4th = 1 pt
    static int points = 0;
    static int totalPoints = 0;
    static int currentPicIdx=0;
    static String leaderBoardName = "leaderboard.json";

    //holds the lowest score on the leaderboard to see if players score qualifies
    static Integer lowest = null;
    static JSONArray boardArr = null;
    static int numSpots=10; // the number of spots on the leaderboard
    static String playerName = "null";
    static int secondsAllowed = 60;

    /**
     * Takes in a player JSONObject of format:
     * {"name": "playerName", "score" : "10"}
     * And adds that player to the leaderboard in the correct position
     * @param player
     */
    public static void addScore(JSONObject player){
        LeaderBoardWriter w = new LeaderBoardWriter();
        JSONArray arr = w.readJSONArrayFromFile(leaderBoardName);
        arr.put(player);
        w.writeJSONArrayToFile(arr,leaderBoardName);
        return;
    }//ends addScore

    /**
     *Gets the lowest value in on the leaderboard
     * @return lowest score on leaderboard
     */
    public static int getLowest(){
        //I assume that we need to create a JSONArray object from the following string
        int min = Integer.MIN_VALUE;
        int answer = 0;
        JSONArray sortedJsonArray = null;
        try {
            LeaderBoardWriter w = new LeaderBoardWriter();

            JSONArray jsonArr = w.readJSONArrayFromFile("leaderboard.json");
            List<JSONObject> jsonValues = new ArrayList<JSONObject>();

            if(jsonArr != null && jsonArr.length() == 0){
                return Integer.MIN_VALUE; //basically set to negative infinity so we know we can add ours
            }else{
                min = Integer.parseInt(jsonArr.getJSONObject(0).getString("score")); //start the lowest at the first in the array
            }
            //add all of the current leaderboard objects to jsonValues
            int val;
            for (int i = 0; i < jsonArr.length(); i++) {
                val = Integer.parseInt(jsonArr.getJSONObject(i).getString("score"));
                if(val < min){min = val;}
            }
            System.out.println("\nmin is\n");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        answer = min;
    return answer;
    }//ends addScore

    /**
     *Creates a JSON object that represents a greeting and prompts the user for their name.
     * @return JSONObject
     */

    /**
     * Creates the basic structure of a request for my application layer protocol, and initializes all values to "null"
     * to begin with.
     * basic structure is:
     *{
     *              "header":
     *              {
     *                     "datatype":"null",
     *                     "type":"null"
     *               },
     *               "payload":
     *               {
     *                     "data":          "null",
     *                     "picture" :      "null",
     *                     "points" :       "null",
     *                     "leaderboard" :  "null"
     *
     *                }
     * }
     * @return JSONObject request template
     */


    public static JSONObject createResponseTemplate(){
        String str = "{\n" +
                "             \"header\":\n" +
                "             {\n" +
                "                    \"datatype\":\"null\",\n" +
                "                    \"type\":\"null\"\n" +
                "              },\n" +
                "              \"payload\":\n" +
                "              {\n" +
                "                    \"data\":          \"null\",\n" +
                "                    \"picture\" :      \"null\",\n" +
                "                    \"points\" :       \"null\",\n" +
                "                    \"leaderboard\" :  \"null\"\n" +
                "     \n" +
                "               }\n" +
                "}";
        return new JSONObject(str);
    }

    /**
     * Calculates the points for the guess.
     * //Uses: 1st quote = 5 pts, 2nd = 4 pts, 3rd = 3pts, 4th = 1 pt
     * Utilizes the currentPicIdx as I show the images in order though the characters are randomized.
     * So if they guess it with picture with index = 0, they guessed first try.
     * @return points to add.
     */
    public static int calculatePoints(){
        int points = 0;
        switch(numGuesses){
            case 0:
                System.out.println("\nFirst try 5 pts\n");
                points=5;
                break;
            case 1:
                System.out.println("\nFirst try 5 pts\n");
                points=5;
                break;
            case 2:
                System.out.println("\nSecond try 4 pts\n");
                points=4;
                break;
            case 3:
                System.out.println("\nThird try 3 pts\n");
                points=3;
                break;
            case 4:
                System.out.println("\nFourth try 1 pt\n");
                points=1;
                break;
            default:
                System.out.println("\nTook you a while! 1 pt\n");
                points=1;
        }
        return points;
    }

    /**
     * Constructs the response for beginning a conversation. Presents the Client with a greeting and prompts for
     * their name.
     * @return JSONObject representing the response from Server to Client
     */
    public static JSONObject begin() {
        JSONObject response = createResponseTemplate();
        String message = "Hey there weary traveler. Think you got what it takes to play the game?\n" +
                "Heh... doubt it. (lights cigar)....\n" +
                "So whats your name anyways... not that its going on the leaderboard....\n" +
                "Enter your name in the input box above or type \"leaderboard\" to see the leaderboard\n";
        response.getJSONObject("header").put("datatype","String");
        response.getJSONObject("header").put("type","begin");
        response.getJSONObject("payload").put("data",message);
        try {
            response = appendImage(response);
        }catch(Exception e){
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Constructs a response to receiving the Client's name, or if the Client typed 'leaderboard',
     * displays the leaderboard. Also starts the timer.
     * @param request JSONObject request
     * @return JSONObject response
     */
    public static JSONObject getName(JSONObject request) {
        //start the timer
        start = LocalTime.now();
        String name = null;
        JSONObject response = createResponseTemplate();
        try{
            if(request.has("payload")){
                JSONObject responsePayload = request.getJSONObject("payload");
                name = String.valueOf(responsePayload.get("data"));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        if(name.equals("leaderboard")){
            gameState = GameState.BEGIN;
            System.out.println("In handleGetName on server side:");
            System.out.println("\nPlayer asked for the leaderboard\n");
            response.getJSONObject("header").put("datatype", "String");
            response.getJSONObject("header").put("type", "getName");
            String message = "\nLEADERBOARD\n";
            //get the leaderBoard as a string
            String str = "";

            LeaderBoardWriter w = new LeaderBoardWriter();
            str = w.readJSONArrayFromFile(leaderBoardName).toString();
            String getNameString = "\n\nIf you wanna play enter your name or type leaderboard again, to see the leaderboard\n";
            str += getNameString;
            message += str;

            //add the leaderboard as the payload
            response.getJSONObject("payload").put("data", message);
        }else {
            System.out.println("In handleGetName on server side:");
            System.out.println("\nServer says the players name is: " + name + "\n");
            playerName = name;

            response.getJSONObject("header").put("datatype", "String");
            response.getJSONObject("header").put("type", "getName");
            String message = "Hey there..." + name + " was it? " + "..that's kind of a weird name.\n" +
                    "well " + name + " good luck! ...you're gonna need it! Here's your first problem\n" +
                    "Guess which character said this ^\n";
            response.getJSONObject("payload").put("data", message);
            gameState = GameState.GUESSING;
        }
        try {
            response = appendImage(response);
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("\nThe correct answer were looking for is: " + currentCharacter.name + "\n");
        return response;
    }//ends getName

    /**
     * Constructs a response to receiving the Client's name.
     * @param request JSONObject request
     * @return JSONObject response
     */
    public static JSONObject guess(JSONObject request) {
        LocalTime now = LocalTime.now();
        Duration elapsed = Duration.between(start, now);
        long elapsedSeconds = elapsed.getSeconds();
        System.out.println("\nIts been:" + elapsedSeconds + "\nseconds since the player was shown the first quote\n");
        numGuesses++;
        JSONObject response = createResponseTemplate();
        String playersGuess = null;
        System.out.println("\nThe correct answer were looking for is: " + currentCharacter.name + "\n");
        try{
            //case: Time out, player loses
            if(elapsedSeconds >= secondsAllowed){
                /////////////////////////////////////////////////////////////////////////////////////////////////
                            gameState = GameState.LOSE;
                            response.getJSONObject("header").put("datatype", "String");
                            response.getJSONObject("header").put("type", "lose");
                            response.getJSONObject("payload").put("data", "Sorry, Times up! You Lose!");
                            response.getJSONObject("payload").put("points", String.valueOf(totalPoints));
                            resetCurrentCharacter();
                /////////////////////////////////////////////////////////////////////////////////////////////////
            }else {

                if (request.has("payload")) {
                    JSONObject requestPayload = request.getJSONObject("payload");
                    playersGuess = String.valueOf(requestPayload.get("data"));
                    System.out.println("In guess on server side:");
                    System.out.println("\nServer says the players guess was: " + playersGuess + "\n");
                    //case: the player guessed correctly
                    if (playersGuess.equalsIgnoreCase(currentCharacter.name)) {
                        correctGuesses++;
                        System.out.println("\nPlayer guessed correctly\n");
                        System.out.println("Correct guesses so far: " + correctGuesses + "\n");
                        int points = calculatePoints();
                        totalPoints += points;
                        ////////////////////////////////////////////////case: the player won////////////////////////////
                        if (correctGuesses == 3) {
                            //add score to the leaderboard
                            String str = "{\"name\": \"" + playerName + "\", \"score\" : \"" + totalPoints + "\"}";
                            JSONObject player = new JSONObject(str);
                            addScore(player);
                            gameState = GameState.WIN;
                            response.getJSONObject("header").put("datatype", "String");
                            response.getJSONObject("header").put("type", "win");
                            response.getJSONObject("payload").put("data", "+ " + points + " points\n" +
                                    "Congratulations, You Won!!!\n Total Points:" + totalPoints + "\n");
                            response.getJSONObject("payload").put("points", String.valueOf(totalPoints));

                        }//ends player won//////////////////////////////////////////////////////////////////////////////
                        //case: player guessed correctly but hasnt won yet.
                        else {
                            gameState = GameState.GUESSING;
                            response.getJSONObject("header").put("datatype", "String");
                            response.getJSONObject("header").put("type", "guess");
                            response.getJSONObject("payload").put("data", "Good Job! +" + points + " Try This one!");
                            response.getJSONObject("payload").put("points", String.valueOf(totalPoints));
                            resetCurrentCharacter();
                            numGuesses=0;
                        }//ends player didn't win yet but guessed correct
                    }//ends player had correct guess
                    //case: player guessed incorrectly:
                    else {
                        numGuesses++;
                        gameState = GameState.GUESSING;
                        response.getJSONObject("header").put("datatype", "String");
                        response.getJSONObject("header").put("type", "guess");
                        response.getJSONObject("payload").put("data", "Sorry, that wasn't it! Guess again!");
                        response.getJSONObject("payload").put("points", String.valueOf(totalPoints));
                    }
                }
            }//ends timer not out
        }catch(Exception e){
            e.printStackTrace();
        }
        try {
            response = appendImage(response);
        }catch(Exception e){
            e.printStackTrace();
        }

        return response;
    }//ends guess


    /**
     * After a User guesses correctly, we remove that character from the map
     * and choose a new character as our current.
     */
    public static void resetCurrentCharacter(){
        //get rid of old character
        if( currentCharacter != null && imageMap.containsKey(currentCharacter.name)){
            imageMap.remove(currentCharacter.name);
        }
        //choose new current character
        if(imageMap.size() > 0){
        int newLength = names.length -1;
        int idx = -1;

            if (currentCharacter != null) {
                //remove the name from names
                for (int i = 0; i < names.length; i++) {
                    if (names[i] == currentCharacter.name) {
                        idx = i;
                    }
                }

                String[] newNames = new String[newLength];
                int counter = 0;
                for (int i = 0; i < names.length; i++) {
                    if (i != idx) {
                        newNames[counter] = names[i];
                        counter++;
                    }
                }
                names = newNames;
            }
        //randomly choose a new character
            //randomize our guess
            int newIdx =  (int) ((Math.random() * (names.length-1)));
            String choice = names[newIdx];
            currentCharacter = imageMap.get(choice);
            System.out.println("\nThe new chosen character is: " + currentCharacter.name + "\n");

            //reset associated values
            points = 0;
            currentPicIdx=0;
        }
    }


    /**
     * After a User guesses correctly, we remove that character from the map
     * and choose a new character as our current.
     */
    public static void pickNext(){
        int newIdx = 0;

        //choose new current character
        if(imageMap.size() > 0){
            int newLength = names.length -1;
            int idx = -1;

            if (currentCharacter != null) {
                //remove the name from names
                for (int i = 0; i < names.length; i++) {
                    if (names[i].equals(currentCharacter.name)) {
                        idx = i;
                    }
                }
                 newIdx = (idx + 1)%names.length;
            }
            String choice = names[newIdx];
            currentCharacter = imageMap.get(choice);
            System.out.println("\nThe new chosen character is: " + currentCharacter.name + "\n");
            //reset associated values
            currentPicIdx=0;
        }
    }

    /**
     * This method should process an image and append it to the jsonObject.
     * @return JSONObject response with an image
     * @throws IOException
     */
    //////////////////////////////////////////////////////////USING THIS ONE FOR IMAGES//////////////////////
    public static JSONObject appendImage(JSONObject obj) throws IOException {
        File file = null;
                //case: player won
        if(gameState==GameState.WIN){
            file = new File("img/win.jpg"); //win and lose are jpg but rest are png
            //case: player lost
        }else if(gameState==GameState.LOSE){
            file = new File("img/lose.jpg");
            //case: anything else (changing image is handled elsewhere)
        }else if(gameState==GameState.BEGIN){
            file = new File("img/hi.png");
            //we need to get the name next
            gameState=GameState.GETNAME;
        }
        else {
            file = new File(currentCharacter.images[currentPicIdx]);
        }

        //read in the file, turn it into a byte array.
        if (!file.exists()) {
            System.err.println("Cannot find file: " + file.getAbsolutePath());
            System.exit(-1);
        }
        // Read in image
        BufferedImage img = ImageIO.read(file);
        byte[] bytes = null;
        //we are writing out a byte[]
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            bytes = out.toByteArray();
        }
        //we then convert it into Base64
        if (bytes != null) {
            Base64.Encoder encoder = Base64.getEncoder();
            if(obj.has("payload")){
                obj.getJSONObject("payload").put("picture",encoder.encodeToString(bytes));
            }
            return obj;
        }
        return error("Unable to save image to byte array");
    }//ends appendImage


    /**
     * This method appears to create a JSONObject response.
     * @return
     * @throws IOException
     */

    /**
     * Constructs a response to receiving the Client's more request.
     * @param request JSONObject request
     * @return JSONObject response
     */
    public static JSONObject more(JSONObject request) {
        //skipping to the next picture counts as a guess
        numGuesses++;
        JSONObject response = createResponseTemplate();
        String playersGuess = null;
        System.out.println("\nThe user wants a new image for: " + currentCharacter.name + "\n");
        try{
            if(request.has("payload")) {
                JSONObject requestPayload = request.getJSONObject("payload");
                playersGuess = String.valueOf(requestPayload.get("data"));
                System.out.println("In guess on server side:");
                //case: the player guessed correctly
                if (currentPicIdx < 3) {
                    currentPicIdx++;
                    gameState=GameState.MORE;
                    response.getJSONObject("header").put("datatype","String");
                    response.getJSONObject("header").put("type","guess");
                    response.getJSONObject("payload").put("data","Heres another one!");
                    response.getJSONObject("payload").put("points",String.valueOf(totalPoints));
                }
               else{
                    gameState=GameState.MORE;
                    response.getJSONObject("header").put("datatype","String");
                    response.getJSONObject("header").put("type","guess");
                    response.getJSONObject("payload").put("data","No more pictures for this character, sorry!");
                    response.getJSONObject("payload").put("points",String.valueOf(totalPoints));
                }
        }  }catch(Exception e){
            e.printStackTrace();
        }

        try{
            response = appendImage(response);
        }catch(Exception e){e.printStackTrace();}
        return response;
    }//ends more

    /**
     * Constructs a response to skip to the next character.
     *  Player Loses 2 points for this choice.
     * @param request JSONObject request
     *
     * @return JSONObject response
     */
    public static JSONObject next(JSONObject request) {
        //were skipping to a new character, so reset the number of guesses to zero
        numGuesses = 0;
        totalPoints-=2;
        JSONObject response = createResponseTemplate();
        response.getJSONObject("payload").put("points",String.valueOf(totalPoints));
        String playersGuess = null;
        System.out.println("\nThe user wants a new image for: " + currentCharacter.name + "\n");
        try{
            pickNext();//chooses the next available Character
            if(request.has("payload")) {
                JSONObject requestPayload = request.getJSONObject("payload");
                playersGuess = String.valueOf(requestPayload.get("data"));
                System.out.println("In guess on server side:");
                //case: the player guessed correctly
                    gameState=GameState.GUESSING;
                    response.getJSONObject("header").put("datatype","String");
                    response.getJSONObject("header").put("type","guess");
                    response.getJSONObject("payload").put("data","\n -2 points\n" +
                                                          "Okay, here's a new Character's quote");
                    response.getJSONObject("payload").put("points",String.valueOf(totalPoints));
                }

            }  catch(Exception e){
            e.printStackTrace();
        }
        try{
            response = appendImage(response);
        }catch(Exception e){e.printStackTrace();}
        return response;
    }//ends next


    public static JSONObject image() throws IOException {
        JSONObject json = new JSONObject();
        json.put("datatype", 2);

        json.put("type", "image");

        File file = new File("img/" + currentCharacter.name + "/quote1.png");
        //read in the file, turn it into a byte array.
        if (!file.exists()) {
            System.err.println("Cannot find file: " + file.getAbsolutePath());
            System.exit(-1);
        }
        // Read in image
        BufferedImage img = ImageIO.read(file);
        byte[] bytes = null;

        //we are writing out a byte[]
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            bytes = out.toByteArray();
        }
        //we then convert it into Base64
        if (bytes != null) {
            Base64.Encoder encoder = Base64.getEncoder();
            json.put("data", encoder.encodeToString(bytes));
            return json;
        }
        return error("Unable to save image to byte array");
    }//ends image

    /**
     * Displays the leaderBoard and packages it into the Client
     * @param request
     * @return
     */
    public static JSONObject leaderBoard(JSONObject request){



        JSONObject response = createResponseTemplate();


        try{

                response.getJSONObject("header").put("datatype","String");
                response.getJSONObject("header").put("type","guess");
                response.getJSONObject("payload").put("data","\n -2 points\n" +
                        "Okay, here's a new Character's quote");
                response.getJSONObject("payload").put("points",String.valueOf(totalPoints));
            }
          catch(Exception e){
            e.printStackTrace();
        }
        try{
            response = appendImage(response);
        }catch(Exception e){e.printStackTrace();}
        return response;
    }

    public static JSONObject error(String err) {
        JSONObject json = new JSONObject();
        json.put("error", err);
        return json;
    }//ends error

    public static void main(String[] args) throws IOException {
        //initialize imageMap with the names of all of their
        //corresponding quote images
        imageMap = new HashMap<String,CharacterGroup>();
        //HashMap<String,CharacterGroup> copyMap = new HashMap<String,CharacterGroup>();
        String[] correctGuesses = {"captain america","darth vader","homer simpson" +
                "jack sparrow","joker","tony stark","wolverine"};

        for(int i = 0; i < names.length; i++){
            CharacterGroup group;
            String[] arr= new String[4];
            // map quote[i] i = 1->4 to array
            for(int j = 0; j < 4; j++){
                int num = j+1;
                arr[j] = "img/" + directoryNames[i] + "/quote" + num + ".png";
            }
            group = new CharacterGroup(names[i],directoryNames[i],arr);
            imageMap.put(names[i],group);
        }
        currentCharacter=null;
        resetCurrentCharacter();

        int port=-1;
        ServerSocket serv = null;
        try {
            try {
                if (args.length >= 1) {
                    port = (Integer) Integer.parseInt(args[0]);
                }else{
                    System.out.println("\nInput of port needed for Server." +
                            "try using gradle runServer\n" +
                            "which will add a default value of -Pport=8080" +
                            "or alternatively add a custom port -Pport with:" +
                            " gradle runServer -Pport=<custom_port>\n");
                    return;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            System.out.println("Server port:" + port);
            serv = new ServerSocket(port);
            // NOTE: SINGLE-THREADED, only one connection at a time

            while (true) {//loop for accepting new clients
                //socket bundles the port and IP abstracting these 2 into one thing. And many times the IP address is implied.
                //Only the port needs to be given.
                Socket sock = null;
                try {
                    //this is where we wait for a new client////////////////////////
                    sock = serv.accept(); // blocking wait
                    String choice = null;

                    //defining server paramenters such as the input and ouput objects we will be communicating through
                    OutputStream out = sock.getOutputStream();
                    InputStream in = sock.getInputStream();
                    while (true) {
                       byte[] messageBytes = NetworkUtils.Receive(in);
                        JSONObject message = JsonUtils.fromByteArray(messageBytes);
                        JSONObject returnMessage=null;
                        System.out.println("Message is" + message.toString());
                        //starts as Message is{"selected":-1}

                        if (message.has("header") ) {

                            JSONObject requestHeader = message.getJSONObject("header");

                            if( requestHeader.has("type") ) {

                                choice = String.valueOf(requestHeader.get("type"));
                                switch (choice) {
                                    case ("begin"):
                                        returnMessage = begin();
                                        break;
                                    case ("getName"):
                                        returnMessage = getName(message);
                                        break;
                                    case ("guess"):
                                        returnMessage = guess(message);
                                        break;
                                    case ("more"):
                                        returnMessage = more(message);
                                        break;
                                    case ("next"):
                                        returnMessage = next(message);
                                        break;
                                    case ("quit"):
                                        returnMessage = next(message);
                                        break;
                                    default:
                                        returnMessage = error("Invalid selection: " + choice + " is not an option");
                                 }//ends switch
                                }//ends if requestType.has(type)

                            } //ends if message has header
                         else {
                            returnMessage = error("Invalid message received in Server");
                        }
                        // we are converting the JSON object we have to a byte[]
                        if(returnMessage==null){
                            System.out.println("\nresponse is null\n");
                        }
                        byte[] output = JsonUtils.toByteArray(returnMessage);
                        NetworkUtils.Send(out, output);

                    }//ends while(true) loop
                } catch (Exception e) {
                    System.out.println("Client disconnect");
                } finally {
                    if (sock != null) {
                        sock.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serv != null) {
                serv.close();
            }
        }
    } //ends psvm
}//ends class Server