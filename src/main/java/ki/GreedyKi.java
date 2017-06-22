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
				Board newBoard = new Board(data.board);
				boolean vertical = shiftPoint.y == 0 || shiftPoint.y == 6;
				boolean rightTop = shiftPoint.y == 0 || shiftPoint.x == 6;
				Point[] tempPos = newBoard.placeShiftCard(shiftCard, shiftPoint, playerPos, treasurePos, vertical,
						rightTop);
				playerPos = tempPos[0];
				treasurePos = tempPos[1];
				if (treasurePos.x < 0 || treasurePos.x > 6 || treasurePos.y < 0 || treasurePos.y > 6)
					break;

				for (Point possibleMove : possibleMoves(playerPos, newBoard)) {
					int distance = getDistance(treasurePos, possibleMove);
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
