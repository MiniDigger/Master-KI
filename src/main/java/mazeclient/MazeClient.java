package mazeclient;

import java.net.Socket;

import mazeclient.generated.MazeCom;

/**
 * Created by mbenndorf on 20.06.2017.
 */
public class MazeClient {

    private Socket socket;
    private UTFInputStream inputStream;
    private UTFOutputStream outputStream;

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
        //TODO handshake
        return false;
    }

    private void send(MazeCom msg) {
        //TODO send msg
    }
}
