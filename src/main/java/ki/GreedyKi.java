package ki;

import mazeclient.MazeClient;
import mazeclient.generated.CardType;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class GreedyKi extends KI {

	public GreedyKi(KiData data, MazeClient client) {
		super(data, client);
	}

	@Override public void move() {
		Point playerPos = data.playerPos;
		Point treasurePos = getPositionOfTreasure();
		if (treasurePos == null) {
			treasurePos = (Point) playerPos.clone();
		}
		boolean treasureOnShift = treasurePos.equals(playerPos);
		// art der karte -> anzahl drehungen
		int rotateCount = isCardIShape(data.card) ? 1 : 3;

		// Karte reinschieben
		int minDistance = Integer.MAX_VALUE;
		Point bestMove = playerPos;
		Point bestShiftPos = null;
		CardType bestShiftCard = null;

		CardType shiftCard = data.card;
		outer:
		for (int i = 0; i < POSTOSHIFTCARD.length; i++) {
			Point shiftPoint = new Point(POSTOSHIFTCARD[i][0], POSTOSHIFTCARD[i][1]);
			if (shiftPoint.equals(data.forbiddenPos))
				continue;
			for (int j = 0; j < rotateCount; j++) {
				Point tempPlayerPos = new Point(playerPos);
				Point tempTreasurePos = treasureOnShift ? new Point(shiftPoint) : new Point(treasurePos);
				Board newBoard = new Board(data.board);
				boolean vertical = shiftPoint.y == 0 || shiftPoint.y == 6;
				boolean rightTop = shiftPoint.y == 0 || shiftPoint.x == 6;
				Point[] tempPos = newBoard
						.placeShiftCard(shiftCard, shiftPoint, tempPlayerPos, tempTreasurePos, vertical, rightTop);
				tempPlayerPos = tempPos[0];
				tempTreasurePos = treasureOnShift ? shiftPoint : tempPos[1];
				if (tempTreasurePos.x < 0 || tempTreasurePos.x > 6 || tempTreasurePos.y < 0 || tempTreasurePos.y > 6)
					break;

				List<PointWeightPair> weightedMap = weightMapDijkstra(tempTreasurePos, newBoard);
				for (Point possibleMove : possibleMoves(tempPlayerPos, newBoard)) {
					int distance = weightedMap.get(weightedMap.indexOf(new PointWeightPair(possibleMove, 0))).weight;
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
		if (bestMove.x == playerPos.x && bestMove.y == playerPos.y) {
			// Gegner verbauen --> suche Shiftcard um Gegner zu ärgern
			System.out.println("Gegner versuchen zu verbauen");
			Map<Integer, Integer> treasuresForPlayer = new HashMap<>();
			int playerWithMostTreasures = -1;
			int mostTreasures = Integer.MAX_VALUE;
			for (int i = 0; i < data.oldTreasures.size(); i++) {
				if (data.oldTreasures.get(i).getPlayer() == MazeClient.playerId) {
					continue;
				}
				if (data.oldTreasures.get(i).getTreasures() < mostTreasures) {
					mostTreasures = data.oldTreasures.get(i).getTreasures();
					playerWithMostTreasures = data.oldTreasures.get(i).getPlayer();
				}
				treasuresForPlayer.put(data.oldTreasures.get(i).getPlayer(), data.oldTreasures.get(i).getTreasures());
			}
			boolean randShift = false;
			Set<Entry<Integer, Point>> entrySet = data.enemies.entrySet();
			for (Entry<Integer, Point> entry : entrySet) {
				int x = entry.getValue().x;
				int y = entry.getValue().y;
				if ((x == 1 || x == 3 || x == 5) || (y == 1 || y == 3 || y == 5)) {
					// ist Gegner am Rand?
					int shiftPosX = x;
					int shiftPosY = y;
					if (x == 0 || x == 6) {
						shiftPosX = (x + 6) % 12;
					}
					if (y == 0 || y == 6) {
						shiftPosY = (y + 6) % 12;
					}
					if (shiftPosX == data.forbiddenPos.x && shiftPosY == data.forbiddenPos.y) {
						continue;
					}
					// wir können Karte reinschieben
					if (shiftPosX != x || shiftPosY != y) {
						randShift = true;
						bestShiftPos = new Point(shiftPosX, shiftPosY);
					}
					// wenn höchste Schatz sabotiert werden kann direkt Schleife
					// verlassen
					if (entry.getKey() == playerWithMostTreasures) {
						break;
					}
				} else {
					continue;
				}
			}
			if (!randShift) {
				// shifte höchstmöglichen Spieler
				// wenn kein Shift möglich, random Shift
				Point p = data.enemies.get(playerWithMostTreasures);
				int x = p.x;
				int y = p.y;
				if ((x == 1 || x == 3 || x == 5) || (y == 1 || y == 3 || y == 5)) {
					// shifte höchsten Spieler
					Point[] possibleShifts = new Point[] { new Point(x, 0), new Point(6, y), new Point(x, 6),
							new Point(0, y) };

					for (Point point : possibleShifts) {
						if (point.x == data.forbiddenPos.x && point.y == data.forbiddenPos.y) {
							continue;
						}
						if (!isLoosePosition(point.x, point.y)) {
							continue;
						}
						bestShiftPos = new Point(point.x, point.y);
					}
				} else {
					int secondPlayer = -1;
					int secondTreasures = Integer.MAX_VALUE;
					for (int i = 0; i < data.oldTreasures.size(); i++) {
						if (data.oldTreasures.get(i).getPlayer() == MazeClient.playerId
								|| data.oldTreasures.get(i).getPlayer() == playerWithMostTreasures) {
							continue;
						}
						if (data.oldTreasures.get(i).getTreasures() < secondTreasures) {
							secondTreasures = data.oldTreasures.get(i).getTreasures();
							secondPlayer = data.oldTreasures.get(i).getPlayer();
						}
					}
					if (secondPlayer != -1) {
						p = data.enemies.get(secondPlayer);
						x = p.x;
						y = p.y;
						if ((x == 1 || x == 3 || x == 5) || (y == 1 || y == 3 || y == 5)) {
							int shiftPosX = x;
							int shiftPosY = 0;
							if (shiftPosX == data.forbiddenPos.x && shiftPosY == data.forbiddenPos.y) {
								shiftPosX = 6;
								shiftPosY = y;
							}
							bestShiftPos = new Point(shiftPosX, shiftPosY);
						} else {
							int lastPlayer = -1;
							int lastTreasures = Integer.MAX_VALUE;
							for (int i = 0; i < data.oldTreasures.size(); i++) {
								if (data.oldTreasures.get(i).getPlayer() == MazeClient.playerId
										|| data.oldTreasures.get(i).getPlayer() == playerWithMostTreasures
										|| data.oldTreasures.get(i).getPlayer() == secondPlayer) {
									continue;
								}
								if (data.oldTreasures.get(i).getTreasures() < lastTreasures) {
									lastTreasures = data.oldTreasures.get(i).getTreasures();
									lastPlayer = data.oldTreasures.get(i).getPlayer();
								}
							}
							if (lastPlayer != -1) {
								p = data.enemies.get(secondPlayer);
								x = p.x;
								y = p.y;
								if ((x == 1 || x == 3 || x == 5) || (y == 1 || y == 3 || y == 5)) {
									// shifte letzten Spieler
									int shiftPosX = x;
									int shiftPosY = 0;
									if (shiftPosX == data.forbiddenPos.x && shiftPosY == data.forbiddenPos.y) {
										shiftPosX = 6;
										shiftPosY = y;
									}
									bestShiftPos = new Point(shiftPosX, shiftPosY);
								}
							}
						}
					}
				}
			}
		}
		client.move(bestMove.x, bestMove.y, bestShiftPos.x, bestShiftPos.y, bestShiftCard);
	}
}
