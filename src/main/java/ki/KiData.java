package ki;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mazeclient.MazeClient;
import mazeclient.generated.AwaitMoveMessageType;
import mazeclient.generated.BoardType;
import mazeclient.generated.BoardType.Row;
import mazeclient.generated.CardType;
import mazeclient.generated.TreasureType;
import mazeclient.generated.TreasuresToGoType;

public class KiData {
	CardType card;
	Board board;
	TreasureType treasure;
	List<TreasuresToGoType> oldTreasures;
	Point forbiddenPos;
	Point playerPos;
	Map<Integer, Point> enemies;

	public KiData(AwaitMoveMessageType data) {
		enemies = new HashMap<>();
		treasure = data.getTreasure();
		oldTreasures = data.getTreasuresToGo();
		initBoard(data.getBoard());
	}

	private void initBoard(BoardType boardType) {
		board = new Board();

		for (int i = 0; i < boardType.getRow().size(); i++) {
			Row row = boardType.getRow().get(i);
			for (int j = 0; j < row.getCol().size(); j++) {
				CardType cardType = row.getCol().get(j);
				Feld feld = new Feld(cardType);
				board.setFeld(j, i, feld);
				if (cardType.getPin().getPlayerID().contains(MazeClient.playerId)) {
					playerPos = new Point(j, i);
				} else {
					for (int k = 0; k < cardType.getPin().getPlayerID().size(); k++) {
						if (cardType.getPin().getPlayerID().get(k) == MazeClient.playerId) {
							continue;
						}
						enemies.put(cardType.getPin().getPlayerID().get(k), new Point(j, i));
					}
				}
			}
		}
		card = boardType.getShiftCard();
		if (boardType.getForbidden() != null) {
			forbiddenPos = new Point(boardType.getForbidden().getRow(), boardType.getForbidden().getCol());
		} else {
			forbiddenPos = new Point(0, 0);
		}
	}

	public void updateBoard(AwaitMoveMessageType data) {
		enemies.clear();
		BoardType boardType = data.getBoard();
		List<TreasuresToGoType> treasuresToGo = data.getTreasuresToGo();
		for (int i = 0; i < 7; i++) {
			Row row = boardType.getRow().get(i);
			for (int j = 0; j < 7; j++) {
				CardType cardType = row.getCol().get(j);
				Feld feld = new Feld(cardType);
				if (cardType.getPin().getPlayerID().contains(MazeClient.playerId)) {
					playerPos = new Point(j, i);
				} else {
					for (int k = 0; k < cardType.getPin().getPlayerID().size(); k++) {
						if (cardType.getPin().getPlayerID().get(k) == MazeClient.playerId) {
							continue;
						}
						enemies.put(cardType.getPin().getPlayerID().get(k), new Point(j, i));
					}
				}
				for (int k = 0; k < treasuresToGo.size(); k++) {
					if (treasuresToGo.get(k).getTreasures() != oldTreasures.get(k).getTreasures()) {
						for (int l = 0; l < cardType.getPin().getPlayerID().size(); l++) {
							if (treasuresToGo.get(k).getPlayer() == cardType.getPin().getPlayerID().get(l)) {
								feld.setTreasureIsCollected(true);
							}
						}
					}
				}
				board.setFeld(j, i, feld);
			}
		}
		treasure = data.getTreasure();
		oldTreasures = treasuresToGo;
		card = boardType.getShiftCard();
		if (boardType.getForbidden() != null) {
			forbiddenPos = new Point(boardType.getForbidden().getCol(), boardType.getForbidden().getRow());
		} else {
			forbiddenPos = new Point(0, 0);
		}
	}

}
