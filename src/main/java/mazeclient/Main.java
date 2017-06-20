package mazeclient;

import java.util.logging.Logger;

/**
 * Created by mbenndorf on 20.06.2017.
 */
public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        MazeClient mazeClient = new MazeClient();
        boolean result = mazeClient.connect("localhost", 5123);
        if (!result) {
            logger.severe("Could not connect!");
            return;
        }

        result = mazeClient.handshake();
    }
}
