package mazeclient;

import mazeclient.generated.TreasureType;

import java.util.logging.Logger;

/**
 * Created by mbenndorf on 20.06.2017.
 */
public class Main {

	private static final Logger logger = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		MazeClient mazeClient = new MazeClient();
		boolean result = mazeClient.connect("localhost", 5123);
		if (!result) {
			logger.severe("Could not connect!");
			mazeClient.disconnect();
			return;
		}

		result = mazeClient.handshake("Martin");
		if (!result) {
			logger.severe("Handshake failed!");
			mazeClient.disconnect();
			return;
		}

		mazeClient.setReadDataHandler((data -> {
			// TODO do stuff with data here
		}));

		mazeClient.setMoveHandler(() -> {
			// TODO do KI here
			mazeClient.move(0, 0, 0, 0, new boolean[4], TreasureType.SYM_01);
		});

		mazeClient.setErrorHandler((msg, expectedType) -> {
			logger.warning("Expected " + expectedType.value() + ", got " + msg.getMcType());
		});

		mazeClient.setWinHandler((winMsg) -> {
			logger.info("Winner is " + winMsg.getWinner().getId() + ":" + winMsg.getWinner().getValue());
		});

		mazeClient.listen();
	}
}
