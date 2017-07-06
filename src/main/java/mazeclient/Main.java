package mazeclient;

import ki.GreedyKi;
import ki.KI;
import ki.KiData;
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
	private static KiData kiData;
	private static KI ki;

	private static CardType shiftCard;

	public static void main(String[] args) {
		String hostname = "localhost";
		if (args.length > 0) {
			hostname = args[0];
		}
		logger.info("Connecting to " + hostname + ":5123");
		MazeClient mazeClient = new MazeClient();
		boolean result = mazeClient.connect(hostname, 5123);
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
			// debug stuff
			shiftCard = data.getBoard().getShiftCard();
			boardRenderer.render(shiftCard, data.getBoard());

			// ki data
			if (kiData == null) {
				kiData = new KiData(data);
			} else {
				kiData.updateBoard(data);
			}
		}));

		mazeClient.setMoveHandler(() -> {
			if (ki == null) {
				ki = new GreedyKi(kiData, mazeClient);
			}
			ki.move();
		});

		// todo debug client (overrides KI!)
		// mazeClient.setMoveHandler(() -> readMoveFromCmd(mazeClient));

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
