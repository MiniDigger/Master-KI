package ki;

import java.awt.Point;
import java.util.List;

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

	public KiData(AwaitMoveMessageType data) {
		initBoard(data.getBoard());
		treasure = data.getTreasure();
		oldTreasures = data.getTreasuresToGo();
	}

	private void initBoard(BoardType boardType) {
		board = new Board();

		for (int i = 0; i < boardType.getRow().size(); i++) {
			Row row = boardType.getRow().get(i);
			for (int j = 0; j < row.getCol().size(); j++) {
				CardType cardType = row.getCol().get(j);
				Feld feld = new Feld(cardType);
				board.setFeld(j, i, feld);
				if (cardType.getPin().getPlayerID().contains(MazeClient.playerId))
					playerPos = new Point(j, i);
			}
		}
		card = boardType.getShiftCard();
		forbiddenPos = new Point(boardType.getForbidden().getRow(), boardType.getForbidden().getCol());
	}

	public void updateBoard(BoardType boardType, List<TreasuresToGoType> treasuresToGo) {
		for (int i = 0; i < 7; i++) {
			Row row = boardType.getRow().get(i);
			for (int j = 0; j < 7; j++) {
				CardType cardType = row.getCol().get(j);
				Feld feld = new Feld(cardType);
				if (cardType.getPin().getPlayerID().contains(MazeClient.playerId))
					playerPos = new Point(j, i);
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
		oldTreasures = treasuresToGo;
		card = boardType.getShiftCard();
		forbiddenPos = new Point(boardType.getForbidden().getRow(), boardType.getForbidden().getCol());
	}

}
