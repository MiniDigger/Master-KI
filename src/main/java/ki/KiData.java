package ki;

import mazeclient.MazeClient;
import mazeclient.generated.*;
import mazeclient.generated.BoardType.Row;

import java.awt.*;
import java.util.List;

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
		if (boardType.getForbidden() != null) {
			forbiddenPos = new Point(boardType.getForbidden().getRow(), boardType.getForbidden().getCol());
		} else {
			forbiddenPos = new Point(0, 0);
		}
	}

	public void updateBoard(AwaitMoveMessageType data) {
		BoardType boardType=data.getBoard();
		List<TreasuresToGoType> treasuresToGo=data.getTreasuresToGo();
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
		treasure=data.getTreasure();
		oldTreasures = treasuresToGo;
		card = boardType.getShiftCard();
		if (boardType.getForbidden() != null) {
			forbiddenPos = new Point(boardType.getForbidden().getCol(), boardType.getForbidden().getRow());
		} else {
			forbiddenPos = new Point(0, 0);
		}
	}

}
