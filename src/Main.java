
/**
 * @author mdsinalpha
 */
public class Main {
    public static void main(String[] args) {
        Game game = new Game("127.0.0.1", 9595);
        if (game.connect_to_server())
            game.start("MHN-AI"); //Write your team name here
    }
}
