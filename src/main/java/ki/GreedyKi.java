package ki;

import java.awt.Point;

import mazeclient.MazeClient;
import mazeclient.generated.CardType;

public class GreedyKi extends KI {

	public GreedyKi(KiData data, MazeClient client) {
		super(data, client);
	}

	@Override
	public void move() {
		Point treasurePos = getPositionOfTreasure();
		Point playerPos = data.playerPos;
		// art der karte -> anzahl drehungen
		int rotateCount = isCardIShape(data.card) ? 1 : 3;

		// Karte reinschieben
		int minDistance = Integer.MAX_VALUE;
		Point bestMove = playerPos;
		Point bestShiftPos = null;
		CardType bestShiftCard = null;

		CardType shiftCard = data.card;
		outer: for (int i = 0; i < POSTOSHIFTCARD.length; i++) {
			Point shiftPoint = new Point(POSTOSHIFTCARD[i][0], POSTOSHIFTCARD[i][1]);
			if (shiftPoint.equals(data.forbiddenPos))
				continue;
			for (int j = 0; j < rotateCount; j++) {
				Point tempPlayerPos = (Point) playerPos.clone();
				Point tempTreasurePos = (Point) treasurePos.clone();
				Board newBoard = new Board(data.board);
				boolean vertical = shiftPoint.y == 0 || shiftPoint.y == 6;
				boolean rightTop = shiftPoint.y == 0 || shiftPoint.x == 6;
				Point[] tempPos = newBoard.placeShiftCard(shiftCard, shiftPoint, tempPlayerPos, tempTreasurePos,
						vertical, rightTop);
				tempPlayerPos = tempPos[0];
				tempTreasurePos = tempPos[1];
				if (tempTreasurePos.x < 0 || tempTreasurePos.x > 6 || tempTreasurePos.y < 0 || tempTreasurePos.y > 6)
					break;

				for (Point possibleMove : possibleMoves(tempPlayerPos, newBoard)) {
					int distance = getDistance(tempTreasurePos, possibleMove);
					if (distance < minDistance) {
						minDistance = distance;
						bestMove = possibleMove;
						bestShiftCard = shiftCard;
						bestShiftPos = shiftPoint;
						if (distance == 0) {
							break outer;
						}
					}
				}

				shiftCard = rotateCard(shiftCard);
			}
		}

		client.move(bestMove.x, bestMove.y, bestShiftPos.x, bestShiftPos.y, bestShiftCard);
	}

	private int getDistance(Point from, Point to) {
		return Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
	}
}
