package mazeclient;

import java.net.Socket;
import java.util.logging.Logger;

import mazeclient.generated.AwaitMoveMessageType;
import mazeclient.generated.LoginMessageType;
import mazeclient.generated.LoginReplyMessageType;
import mazeclient.generated.MazeCom;
import mazeclient.generated.MazeComType;
import mazeclient.generated.MoveMessageType;

/**
 * Created by mbenndorf on 20.06.2017.
 */
public class MazeClient {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private Socket socket;
    private UTFInputStream inputStream;
    private UTFOutputStream outputStream;

    private boolean doNextMove = true;

    private int playerId;

    public MazeClient() {
        //TODO setup marshaller/unmarshaller
    }

    public boolean connect(String hostname, int port) {
        try {
            socket = new Socket(hostname, port);
            inputStream = new UTFInputStream(socket.getInputStream());
            outputStream = new UTFOutputStream(socket.getOutputStream());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean handshake() {
        // send login msg
        MazeCom mazeCom = null;//TODO
        LoginMessageType loginMessageType = null;//TODO
        loginMessageType.setName("Martin");
        mazeCom.setLoginMessage(loginMessageType);
        mazeCom.setMcType(MazeComType.LOGIN);
        send(mazeCom);
        // read login reply msg
        mazeCom = read();
        if (mazeCom.getMcType() != MazeComType.LOGINREPLY) {
            logger.warning("Expected LOGINREPLY, got " + mazeCom.getMcType() + "!");
            return false;
        }
        LoginReplyMessageType reply = mazeCom.getLoginReplyMessage();
        playerId = reply.getNewID();
        logger.info("We are ID " + playerId);
        return true;
    }

    public void listen(MoveHandler handler) {
        while (doNextMove) {
            // read packet
            MazeCom msg = read();
            if (msg.getMcType() != MazeComType.AWAITMOVE) {
                logger.warning("Expected AWAITMOVE, got " + msg.getMcType() + "!");
                continue;
            }

            // save data
            AwaitMoveMessageType awaitMoveMsg = msg.getAwaitMoveMessage();
            //TODO do stuff with the data

            // do our moves, we got 3 tries
            for (int i = 0; i < 3; i++) {
                // send move
                MazeCom mazeCom = null; //TODO
                MoveMessageType moveMsg = handler.move(this);
                mazeCom.setMoveMessage(moveMsg);
                mazeCom.setMcType(MazeComType.MOVE);
                send(mazeCom);

                // check if move was ok
                //TODO read reply
            }
        }
    }

    private void send(MazeCom msg) {
        //TODO send msg
    }

    private MazeCom read() {
        // TODO read msg
        return null;
    }

    @FunctionalInterface
    interface MoveHandler {

        MoveMessageType move(MazeClient context);
    }
}
