package ki;

import java.awt.Point;

import mazeclient.generated.CardType;

public class GreedyKi extends KI {

	public GreedyKi(KiData data) {
		super(data);
	}

	@Override
	void move() {
		Point treasurePos = getPositionOfTreasure();
		Point playerPos=data.playerPos;
		// art der karte -> anzahl drehungen
		int rotateCount = isCardIShape(data.card) ? 1 : 3;

		// Karte reinschieben
		CardType shiftCard = data.card;
		for (int i = 0; i < POSTOSHIFTCARD.length; i++) {
			Point shiftPoint = new Point(POSTOSHIFTCARD[i][0], POSTOSHIFTCARD[i][1]);
			if (shiftPoint.equals(data.forbiddenPos))
				break;
			for (int j = 0; j < rotateCount; j++) {
				Board newBoard = new Board(data.board);
				boolean vertical = shiftPoint.y == 0 || shiftPoint.y == 6;
				boolean rightTop = shiftPoint.y == 0 || shiftPoint.x == 6;
				newBoard.placeShiftCard(shiftCard, shiftPoint, vertical, rightTop);
				
				
				
				shiftCard = rotateCard(shiftCard);
			}
		}
	}
}
