package Assignment3Starter;
//import statements
import org.json.*;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.ImageIO;
//import org.apache.commons.io.*;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status. 
 * 
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 *     -> modal means that it opens the GUI and suspends background processes. Processing 
 *        still happens in the GUI. If it is desired to continue processing in the 
 *        background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 *Added from AdvancedCustomProtocol
 *
 *request: { "selected": <int: 1=joke, 2=quote, 3=image, 4=random>,
 * (optional)"min": <int>, (optional)"max":<int> }
 *
 *response: {"datatype": <int: 1-string, 2-byte array>, "type": <"joke",
 * "quote", "image"> "data": <thing to return> }
 *
 *error response: {"error": <error string> }
 *
 */
public class ClientGui implements Assignment3Starter.OutputPanel.EventHandlers {
  JDialog frame;
  PicturePanel picturePanel;
  OutputPanel outputPanel;
  int port;
  String host;
  Socket sock;
  OutputStream out;
  InputStream in;
  Boolean hasName = false;
  enum GameState {
    GUESS,
    WIN,
    LOSE,
    GETNAME,
    NEXT
  }
  GameState gameState = GameState.GETNAME;

  /**
   * Creates the basic structure of a request for my application layer protocol, and initializes all values to "null"
   * to begin with.
   * basic structure is:
   * {
   * "header":
   * {
   * "datatype":"null",
   * "type":"null"
   * },
   * "payload":
   * {
   * "data":"null"
   * }
   * <p>
   * }
   *
   * @return JSONObject request template
   */
  public static JSONObject createRequestTemplate() {
    String str = "{\n" +
            " \"header\":\n" +
            "    {\n" +
            "        \"datatype\":\"null\",\n" +
            "        \"type\":\"null\"\n" +
            "    },\n" +
            " \n" +
            " \"payload\":\n" +
            " {\n" +
            "    \"data\":\"null\" \n" +
            " }\n" +
            "}";
    return new JSONObject(str);
  }


  /**
   * Returns a response JSONObject for the begin state. This just tells the
   * server that the client is ready.
   *
   * @return JSONObject representing a request
   */
  public static JSONObject begin() {
    JSONObject request;
    //String str2 = "{\"selected\":-1}";
    //creates the initial structure of the request.
    request = createRequestTemplate(); //creates a JSON object with the basic structure of my reply with all null values.
    request.getJSONObject("header").put("datatype", "String");
    request.getJSONObject("header").put("type", "begin");
    request.getJSONObject("payload").put("data", "empty message");
    System.out.println("request object on client side after creation:");
    System.out.println("\n" + request.toString() + "\n");
    return request;
  }

  /**
   * Extracts the payload data from the response for the 'begin' action and performs the corresponding code.
   *
   * @param response
   */
  public void handleBegin(JSONObject response) {
    try {
      if (response.has("payload")) {
        JSONObject responsePayload = response.getJSONObject("payload");
        String message = String.valueOf(responsePayload.get("data"));
        System.out.println("In handleBegin");
        System.out.println("\nThe response payload data is:");
        System.out.println("\n" + message + "\n");
        outputPanel.appendOutput(message);
        addImage(response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }//ends handleBegin

  /**
   * @return JSONObject representing a request
   */
  public static JSONObject getName(String name) {
    JSONObject request;
    //String str2 = "{\"selected\":-1}";
    //creates the initial structure of the request.
    request = createRequestTemplate(); //creates a JSON object with the basic structure of my reply with all null values.
    request.getJSONObject("header").put("datatype", "String");
    request.getJSONObject("header").put("type", "getName");
    request.getJSONObject("payload").put("data", name);
    System.out.println("getName Object on Client side after creation:");
    //System.out.println("\n" + request.toString() + "\n");
    return request;
  }

  /**
   * Extracts the payload data from the response for the 'getName' action and performs the corresponding code.
   *
   * @param response
   */
  public void handleGetName(JSONObject response) {
    try {
      if (response.has("payload")) {
        JSONObject responsePayload = response.getJSONObject("payload");
        String message = String.valueOf(responsePayload.get("data"));
        System.out.println("In handleGetName");
        System.out.println("\nThe response payload`s data is:");
        System.out.println("\n" + message + "\n");
        outputPanel.appendOutput(message);
        ////////////////////TODO: parse and display image/////////////////////////
        addImage(response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }//ends handleBegin

  public void addImage(JSONObject obj) {
    try {
      System.out.println("Your image");
      Base64.Decoder decoder = Base64.getDecoder();

      try {
        if (obj.has("payload")) {
          JSONObject payload = obj.getJSONObject("payload");
          byte[] bytes = decoder.decode(String.valueOf(payload.get("picture")));
          ImageIcon icon = null;
          try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            picturePanel.insertImage(bais, 0, 0);
          } catch (Exception e) {
            System.out.println("\nIssue in my addImage method\n");
            e.printStackTrace();
          }

        }//ends if obj.has(payload)
      } catch (Exception e) {
        System.out.println("\nError looking for the image in addImage\n");
        e.printStackTrace();
      }
    } catch (Exception e) {
      System.out.println("\nError in the addImage method\n");
      e.printStackTrace();
    }
  }//ends addImage

  /**
   * Returns a response JSONObject for the guess state. This just sends the server a guess.
   *
   * @return JSONObject representing a guess request
   */
  public static JSONObject guess(String input) {
    JSONObject request;
    //String str2 = "{\"selected\":-1}";
    //creates the initial structure of the request.
    request = createRequestTemplate(); //creates a JSON object with the basic structure of my reply with all null values.
    request.getJSONObject("header").put("datatype", "String");
    request.getJSONObject("header").put("type", "guess");
    request.getJSONObject("payload").put("data", input);
    System.out.println("request object on client side after creation:");
    System.out.println("\n" + request.toString() + "\n");
    return request;
  }

  /**
   * Extracts the payload data from the response for the 'getName' action and performs the corresponding code.
   *
   * @param response
   */

  public void handleGuess(JSONObject response) {
    try {
      if (response.has("payload")) {
        JSONObject responsePayload = response.getJSONObject("payload");
        String message = String.valueOf(responsePayload.get("data"));
        System.out.println("In handleGetName");
        System.out.println("\nThe response payload is:");
        System.out.println("\n" + message + "\n");
        int newTotalPoints = Integer.parseInt(responsePayload.getString("points"));
        outputPanel.setPoints(newTotalPoints);
        outputPanel.appendOutput(message);


        addImage(response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }//ends handleBegin

  /**
   * Returns a response JSONObject for the guess state. This just sends the server a guess.
   *
   * @return JSONObject representing a guess request
   */
  public static JSONObject more() {
    JSONObject request;
    //String str2 = "{\"selected\":-1}";
    //creates the initial structure of the request.
    request = createRequestTemplate(); //creates a JSON object with the basic structure of my reply with all null values.
    request.getJSONObject("header").put("datatype", "String");
    request.getJSONObject("header").put("type", "more");
    request.getJSONObject("payload").put("data", "null");
    System.out.println("request object on client side after creation:");
    System.out.println("\n" + request.toString() + "\n");
    return request;
  }

  /**
   * Returns a response JSONObject for the guess state. This just sends the server a guess.
   *
   * @return JSONObject representing a guess request
   */
  public static JSONObject next() {
    JSONObject request;
    //String str2 = "{\"selected\":-1}";
    //creates the initial structure of the request.
    request = createRequestTemplate(); //creates a JSON object with the basic structure of my reply with all null values.
    request.getJSONObject("header").put("datatype", "String");
    request.getJSONObject("header").put("type", "next");
    request.getJSONObject("payload").put("data", "null");
    System.out.println("request object on client side after creation:");
    System.out.println("\n" + request.toString() + "\n");
    return request;
  }

  public void handleEnd(JSONObject response) {
    try {
      if (response.has("payload")) {
        JSONObject responsePayload = response.getJSONObject("payload");
        String message = String.valueOf(responsePayload.get("data"));
        System.out.println("In handleGetName");
        System.out.println("\nThe response payload is:");
        System.out.println("\n" + message + "\n");
        outputPanel.appendOutput(message);
        int newTotalPoints = Integer.parseInt(responsePayload.getString("points"));
        outputPanel.setPoints(newTotalPoints);
        addImage(response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }//ends handleBegin
  /**
   * Construct dialog
   */
  public ClientGui() {
    frame = new JDialog();
    frame.setLayout(new GridBagLayout());
    frame.setMinimumSize(new Dimension(500, 500));
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    // setup the top picture frame
    picturePanel = new PicturePanel();
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.weighty = 0.25;
    frame.add(picturePanel, c);

    // setup the input, button, and output area
    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 1;
    c.weighty = 0.75;
    c.weightx = 1;
    c.fill = GridBagConstraints.BOTH;
    outputPanel = new OutputPanel();
    outputPanel.addEventHandlers(this);
    frame.add(outputPanel, c);
  }

  /**
   * Shows the current state in the GUI
   *
   * @param makeModal - true to make a modal window, false disables modal behavior
   */
  public void show(boolean makeModal) {
    frame.pack();
    frame.setModal(makeModal);
    frame.setVisible(true);
  }
  /**
   * Creates a new game and set the size of the grid
   * Initializes Client side parameters such as socket, InputStream and OutputStream
   * also defines client parameters and performs initial exchange of prompting the Server to ask for Client's name.
   *
   * @param dimension - the size of the grid will be dimension x dimension
   */



  public void newGame(int dimension) {
    picturePanel.newGame(dimension);
    outputPanel.appendOutput("Started new game with a " + dimension + "x" + dimension + " board.");
    outputPanel.setPoints(0);
    //open the socket
    //declaring parameters for connection
    sock = null;
    Boolean connected = false;
    out = null;
    try {
      //create socket combining host and port
      sock = new Socket(host, port);
      out = sock.getOutputStream();
      in = sock.getInputStream();
      //create an object output writer
      //guess is the string we received from the input field
      JSONObject request = null;
      request = begin();
      ///sending out the request
      //casts the ouput to a byteArray to be sent over the wire
      System.out.println("Sending over first request to prompt greeting\n");
      //convert from JSON to bytes and send the bytes over the wire
      //SEND OVER BEGIN REQUEST/////////////////////////////////////////////////////
      NetworkUtils.Send(out, JsonUtils.toByteArray(request));
      System.out.println("Receiving response from Server\n");
      byte[] responseBytes = NetworkUtils.Receive(in);
      //convert the response from bytes back to JSON
      JSONObject response = JsonUtils.fromByteArray(responseBytes);
      if (response.has("error")) {
        System.out.println(response.getString("error"));
      } else {
        ///////////////////////////////////////////////////////////////////////////////////////////
        try {
          String choice = null;
          if (response.has("header")) {
            JSONObject responseHeader = response.getJSONObject("header");
            System.out.println("\nThe header Client received from the server is:\n");
            System.out.println(String.valueOf(responseHeader));
            responseHeader.getString("datatype");
            System.out.println("Your " + responseHeader.getString("type"));
            String data = null;
            if (responseHeader.has("type")) {
              choice = String.valueOf(responseHeader.get("type"));
              switch (choice) {
                case ("begin"):
                  handleBegin(response);
                  break;
                case ("getName"):
                  handleGetName(response);
                  break;
                case ("guess"):
                  System.out.println("\nClient take a guess\n");
                  handleGuess(response);
                  break;
                case ("3"):
                  System.out.println("\nThis code is under construction in Client... \n");
                  break;
                case ("4"):
                  System.out.println("\nThis code is under construction in Client ... \n");
                  break;
                default:
                  System.out.println("\nThis code is under construction in Client... \n");
              }//ends switch
            }//ends if requestType.has(type)

          } //ends if message has header
          else {
            System.out.println("Error receiving message back from server");
          }
        } catch (Exception e) {
          System.out.println("Error on Client side after receiving data from Server");
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      }//ends large else block
    } catch (Exception e) {
      e.printStackTrace();
    }//ends catch
  }//ends newGame

  /**
   * Insert an image into the grid at position (col, row)
   *
   * @param filename - filename relative to the root directory
   * @param row      - the row to insert into
   * @param col      - the column to insert into
   * @return true if successful, false if an invalid coordinate was provided
   * @throws IOException An error occured with your image file
   */
  public boolean insertImage(String filename, int row, int col) throws IOException {
    String error = "";
    try {
      // insert the image
      if (picturePanel.insertImage(filename, row, col)) {
        // put status in output
        outputPanel.appendOutput("Inserting " + filename + " in position (" + row + ", " + col + ")");
        return true;
      }
      error = "File(\"" + filename + "\") not found.";
    } catch (PicturePanel.InvalidCoordinateException e) {
      // put error in output
      error = e.toString();
    }
    outputPanel.appendOutput(error);
    return false;
  }
  /**
   * This method receives input from the user via the outputPanel.
   * It casts the input to lowercase, resets the ouputPanel`s inputText,
   * and passes on the userInput to connect() which decides what to do with it.
   */
  @Override
  public void submitClicked() {
    String userInput = outputPanel.getInputText().toLowerCase();
    // if has input
    if (userInput.length() > 0) {
      // append input to the output panel
      outputPanel.appendOutput(userInput);
      // clear input text box
      outputPanel.setInputText("");
      connect(userInput);
    }
  }

  /**
   * Connect is going to be the main jumping off point to handle
   * logic after a submit event.
   *
   * @param userInput -3
   */
  public void connect(String userInput) {
    try {
      JSONObject request = null;
      if (gameState == GameState.GETNAME) {
        request = getName(userInput);
        gameState = GameState.GUESS;
      } else if (userInput.equalsIgnoreCase("more")) {
        request = more();
      } else if (userInput.equalsIgnoreCase("next")) {
        request = next();
      } else if (gameState == GameState.GUESS) {
        request = guess(userInput);
      }
      if (request == null) {
        System.out.println("Request is null\n\n");
      }
      //decide what type of request to build based on user input
      ///sending out the request
      //casts the ouput to a byteArray to be sent over the wire
      System.out.println("Sending over request generated from from submit event\n");
      //convert from JSON to bytes and send the bytes over the wire
      NetworkUtils.Send(out, JsonUtils.toByteArray(request));
      System.out.println("Receiving response from Server\n");
      byte[] responseBytes = NetworkUtils.Receive(in);
      //convert the response from bytes back to JSON
      JSONObject response = JsonUtils.fromByteArray(responseBytes);
      //System.out.println("Response from server is" + response.toString());
      if (response.has("error")) {
        System.out.println(response.getString("error"));
      } else {
        ///////////////////////////////////////////////////////////////////////////////////////////
        try {
          String choice = null;
          if (response.has("header")) {

            JSONObject responseHeader = response.getJSONObject("header");
            responseHeader.getString("datatype");
            System.out.println("In submit clicked I see header response from server:\n" + String.valueOf(responseHeader) + "\n");
            System.out.println("Your " + responseHeader.getString("type"));
            String data = null;
            if (responseHeader.has("type")) {
              choice = String.valueOf(responseHeader.get("type"));
              switch (choice) {
                case ("begin"):
                  handleBegin(response);
                  break;
                case ("getName"):
                  handleGetName(response);
                  break;
                case ("guess"):
                  handleGuess(response);
                  break;
                case ("win"):
                  handleEnd(response);
                  break;
                case ("lose"):
                  handleEnd(response);
                  break;
                default:
                  System.out.println("\nThis code is under construction in Client submitClicked/connect... \n");
              }//ends switch
            }//ends if requestType.has(type)

          } //ends if message has header
          else {
            System.out.println("Error receiving message back from server");
          }
        } catch (Exception e) {
          System.out.println("Error on Client side after receiving data from Server");
        } finally {
          if (sock != null) {
            //sock.close();
          }
        }//ends finally
        ///////////////////////////////////////////////////////////////////////////////////////////
      }//ends large else block
    } catch (Exception e) {
      e.printStackTrace();
    }//ends catch
  }//ends connect

  /**
   * Key listener for the input text box
   * <p>
   * Change the behavior to whatever you need
   */
  @Override
  public void inputUpdated(String input) {
    if (input.equals("surprise")) {
      outputPanel.appendOutput("You found me!");
    }
  }

  public static void main(String[] args) throws IOException {
    // create the frame
    ClientGui main = new ClientGui();
    // setup the UI to display on image

    try {
      if (args.length >= 2) {
        main.port = (Integer) Integer.parseInt(args[0]);
        main.host = args[1];
      } else {
        System.out.println("\nInput of port and host needed." +
                "try using gradle runClient\n" +
                "which will add default values of -Pport=8080 and -Phost=localhost\n");
        return;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("host:" + main.host);
    System.out.println("port:" + main.port);
    main.newGame(1);
    // add images to the grid
    //main.insertImage("img/hi.png", 0, 0);
    System.out.println("Hello");
    // show the GUI dialog as modal
    main.show(true); // you should not have your logic after this. You main logic should happen whenever "submit" is clicked
    System.out.println("Hello");
    //main logic is in connect() which is called from submitClicked()
  }
}


