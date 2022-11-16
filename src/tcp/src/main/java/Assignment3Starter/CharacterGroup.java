package Assignment3Starter;

/**
 * Holds meta information about the different characters the user is trying to guess.
 *
 * imageMap structure:
 * key: "captain america"
 * value:
 * CharacterGroup{
 *     name: "captain america"
 *     guessesLeft: 4
 *     images[] = {"images/Captain_America/quote1.png"};
 * }
 */
class CharacterGroup{
    String name;
    String directoryName;
    int guessesLeft;
    String[] images;

    //constructor
    CharacterGroup(String name,String directoryName, String[] images){
        this.name = name;
        this.directoryName=directoryName;
        this.guessesLeft=4;
        this.images =images;


    }
}//ends CharacterGroup