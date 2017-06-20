package mazeclient;

import mazeclient.generated.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mbenndorf on 20.06.2017.
 */
public class MazeClient {

	private static final Logger logger = Logger.getLogger(Main.class.getName());

	private Socket socket;
	private UTFInputStream inputStream;
	private UTFOutputStream outputStream;
	private ObjectFactory objectFactory;
	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

	private ErrorHandler errorHandler = (msg, expected) -> {
	};
	private WinHandler winHandler = (msg) -> {
	};
	private MoveHandler moveHandler = () -> {
	};
	private ReadDataHandler readDataHandler = (data) -> {
	};

	private boolean doNextMove = true;

	private int playerId = -1;
	private int moveTry = 0;

	public MazeClient() {
		objectFactory = new ObjectFactory();
		try {
			jaxbContext = JAXBContext.newInstance(MazeCom.class);
			marshaller = jaxbContext.createMarshaller();
			unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
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

	public void disconnect() {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException ignored) {
			}
		}
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException ignored) {
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ignored) {
			}
		}
	}

	public boolean handshake(String name) {
		// send login msg
		LoginMessageType loginMessageType = objectFactory.createLoginMessageType();
		loginMessageType.setName(name);
		MazeCom mazeCom = objectFactory.createMazeCom();
		mazeCom.setLoginMessage(loginMessageType);
		mazeCom.setMcType(MazeComType.LOGIN);
		send(mazeCom);
		// read login reply msg
		mazeCom = read();
		if (mazeCom.getMcType() != MazeComType.LOGINREPLY) {
			errorHandler.handle(mazeCom, MazeComType.LOGINREPLY);
			return false;
		}
		LoginReplyMessageType reply = mazeCom.getLoginReplyMessage();
		playerId = reply.getNewID();
		logger.info("We are ID " + playerId);
		return true;
	}

	public void listen() {
		while (doNextMove) {
			// read packet
			MazeCom msg = read();
			if (msg.getMcType() == MazeComType.WIN) {
				winHandler.handle(msg.getWinMessage());
			}
			if (msg.getMcType() != MazeComType.AWAITMOVE) {
				errorHandler.handle(msg, MazeComType.AWAITMOVE);
				continue;
			}
			moveTry++;
			// save data
			AwaitMoveMessageType awaitMoveMsg = msg.getAwaitMoveMessage();
			readDataHandler.handleData(awaitMoveMsg);

			// send move (handler has to call move!)
			moveHandler.doMove();

			// check if move was ok
			MazeCom mazeCom = read();
			if (mazeCom.getMcType() != MazeComType.ACCEPT) {
				errorHandler.handle(msg, MazeComType.ACCEPT);
				continue;
			}

			AcceptMessageType acceptMessage = mazeCom.getAcceptMessage();
			if (acceptMessage.isAccept()) {
				// doMove was ok, we are done here
				moveTry = 0;
				break;
			} else {
				// try another move
				logger.warning(
						"move was not accepted by server (try " + moveTry + "/3): " + acceptMessage.getErrorCode()
								.name() + " " + acceptMessage.getErrorCode().value());
			}
		}
	}

	public void move(int playerX, int playerY, int cardX, int cardY, CardType shiftCard) {
		MazeCom mazeCom = objectFactory.createMazeCom();
		MoveMessageType moveMsg = objectFactory.createMoveMessageType();
		PositionType playerPos = objectFactory.createPositionType();
		playerPos.setCol(playerX);
		playerPos.setRow(playerY);
		PositionType cardPos = objectFactory.createPositionType();
		cardPos.setCol(cardX);
		cardPos.setRow(cardY);

		moveMsg.setNewPinPos(playerPos);
		moveMsg.setShiftCard(shiftCard);
		moveMsg.setShiftPosition(cardPos);

		mazeCom.setMoveMessage(moveMsg);
		mazeCom.setMcType(MazeComType.MOVE);
		send(mazeCom);
	}

	private void send(MazeCom msg) {
		try {
			String xml = messageToXMLString(msg);
			logger.info("Sending: " + xml);
			outputStream.writeUTF8(xml);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while sending msg", e);
		}
	}

	private MazeCom read() {
		MazeCom msg = null;
		try {
			String xml = inputStream.readUTF8();
			logger.info("Reading: " + xml);
			msg = xmlStringToMessage(xml);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while reading msg", e);
		}
		return msg;
	}

	private MazeCom xmlStringToMessage(String xml) throws JAXBException {
		StringReader sr = new StringReader(xml);
		return (MazeCom) unmarshaller.unmarshal(sr);
	}

	private String messageToXMLString(MazeCom message) throws JAXBException {
		StringWriter sw = new StringWriter();
		marshaller.marshal(message, sw);
		return sw.toString();
	}

	public void setMoveHandler(MoveHandler moveHandler) {
		this.moveHandler = moveHandler;
	}

	public void setReadDataHandler(ReadDataHandler readDataHandler) {
		this.readDataHandler = readDataHandler;
	}

	public void setWinHandler(WinHandler winHandler) {
		this.winHandler = winHandler;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getMoveTry() {
		return moveTry;
	}

	@FunctionalInterface interface MoveHandler {

		void doMove();
	}

	@FunctionalInterface interface ReadDataHandler {

		void handleData(AwaitMoveMessageType data);
	}

	@FunctionalInterface interface ErrorHandler {
		void handle(MazeCom msg, MazeComType expected);
	}

	@FunctionalInterface interface WinHandler {
		void handle(WinMessageType msg);
	}
}
