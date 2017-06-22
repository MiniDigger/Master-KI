package mazeclient;

import mazeclient.generated.CardType;

import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Created by mbenndorf on 20.06.2017.
 */
public class Main {

	private static final Logger logger = Logger.getLogger(Main.class.getName());

	private static Scanner scanner = new Scanner(new InputStreamReader(System.in));

	private static BoardRenderer boardRenderer = new BoardRenderer();

	private static CardType shiftCard;

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
			shiftCard = data.getBoard().getShiftCard();
			boardRenderer.render(shiftCard, data.getBoard());
		}));

		mazeClient.setMoveHandler(() -> {
			// TODO do KI here
			mazeClient.move(0, 0, 0, 0, shiftCard);
		});

		// todo debug client (overrides KI!)
		mazeClient.setMoveHandler(() -> readMoveFromCmd(mazeClient));

		mazeClient.setErrorHandler((msg, expectedType) -> {
			logger.warning("Expected " + expectedType.value() + ", got " + msg.getMcType());
		});

		mazeClient.setWinHandler((winMsg) -> {
			logger.info("Winner is " + winMsg.getWinner().getId() + ":" + winMsg.getWinner().getValue());
		});

		mazeClient.listen();
	}

	private static void readMoveFromCmd(MazeClient mazeClient) {
		logger.info("Please input move...");
		String[] args = scanner.next().split(",");
		mazeClient.move(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[0]),
				Integer.parseInt(args[1]), shiftCard);
	}
}
