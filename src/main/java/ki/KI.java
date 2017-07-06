package ki;

import mazeclient.MazeClient;
import mazeclient.generated.CardType;
import mazeclient.generated.CardType.Openings;
import mazeclient.generated.ObjectFactory;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

public abstract class KI {

	protected KiData data;
	protected final int[][] POSTOSHIFTCARD = new int[][] { { 1, 0 }, { 3, 0 }, { 5, 0 }, { 6, 1 }, { 6, 3 }, { 6, 5 },
			{ 5, 6 }, { 3, 6 }, { 1, 6 }, { 0, 5 }, { 0, 3 }, { 0, 1 } };
	protected ObjectFactory of;
	protected MazeClient client;

	final int WALLWEIGHT = 3;
	final int SPACEWEIGHT = 1;
	final int TREASUREWALLWEIGHT = 5;
	final int FIXEDCARDBONUS = 2;
	final int NEXTTOTREASUREWEIGHT = 3;

	Point getPositionOfTreasure() {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				if (data.board.getFeld(j, i).getTreasure() == data.treasure) {
					return new Point(j, i);
				}
			}
		}
		if (data.treasure.equals(data.card.getTreasure()))
			return data.playerPos;
		System.out.println("no treasure found. WHO STOLE MY TREASURE?");
		return null;
	}

	public KI(KiData data, MazeClient client) {
		this.data = data;
		this.client = client;
	}

	public boolean isCardIShape(CardType card) {
		Openings openings = card.getOpenings();
		return (openings.isTop() && openings.isBottom() && !openings.isLeft() && !openings.isRight())
				|| (!openings.isTop() && !openings.isBottom() && openings.isLeft() && openings.isRight());
	}

	/**
	 * dreht Karte um 90° im Uhrzeigersinn
	 *
	 * @param card
	 * @return
	 */
	public CardType rotateCard(CardType card) {
		if (of == null)
			of = new ObjectFactory();
		CardType newCard = of.createCardType();
		Openings op = of.createCardTypeOpenings();
		op.setTop(card.getOpenings().isTop());
		op.setRight(card.getOpenings().isRight());
		op.setBottom(card.getOpenings().isBottom());
		op.setLeft(card.getOpenings().isLeft());
		newCard.setOpenings(op);
		newCard.setPin(card.getPin());
		newCard.setTreasure(card.getTreasure());

		Openings openings = newCard.getOpenings();
		boolean rightOpening = openings.isRight();
		openings.setRight(openings.isTop());
		openings.setTop(openings.isLeft());
		openings.setLeft(openings.isBottom());
		openings.setBottom(rightOpening);
		newCard.setOpenings(openings);
		return newCard;
	}

	public Set<Point> possibleMoves(Point pos, Board board) {
		Set<Point> moves = new HashSet<>();
		moves.add(pos);

		Queue<Point> positions = new LinkedList<>();
		positions.add(pos);
		boolean[][] visited = new boolean[7][7];
		visited[pos.y][pos.x] = true;

		while (!positions.isEmpty()) {
			Point playerPos = positions.poll();
			for (Point p : getNeighbors(playerPos, board)) {
				if (visited[p.y][p.x])
					continue;
				visited[p.y][p.x] = true;
				moves.add(p);
				positions.add(p);
			}
		}
		return moves;
	}

	public List<PointWeightPair> weightMapDijkstra(Point treasurePos, Board board) {
		List<PointWeightPair> weightedList = new ArrayList<>();
		Queue<PointWeightPair> queue = new PriorityQueue<>();
		PointWeightPair pwp = new PointWeightPair(treasurePos, 0);
		queue.add(pwp);
		weightedList.add(pwp);
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				if (treasurePos.x == i && treasurePos.y == j)
					continue;
				pwp = new PointWeightPair(new Point(i, j), Integer.MAX_VALUE);
				queue.add(pwp);
				weightedList.add(pwp);
			}
		}
		while (!queue.isEmpty()) {
			PointWeightPair act = queue.poll();
			for (PointWeightPair pair : getWeightedNeighbors(act, board, treasurePos)) {
				PointWeightPair toCompare = weightedList.get(weightedList.indexOf(pair));
				if (toCompare.weight > pair.weight) {
					toCompare.weight = pair.weight;
					queue.remove(toCompare);
					queue.add(toCompare);
				}
			}
		}

		for (int i = 0; i < weightedList.size(); i++) {
			PointWeightPair pw = weightedList.get(i);
			Point pos = pw.point;
			// bonus for fixed cards
			if (pos.x % 2 == 0 && pos.y % 2 == 0 && pw.weight > FIXEDCARDBONUS) {
				pw.weight -= FIXEDCARDBONUS;
			}
			// malus for position next to treasure
			if (Math.abs(pos.x - treasurePos.x) + Math.abs(pos.y - treasurePos.y) == 1) {
				pw.weight += NEXTTOTREASUREWEIGHT;
			}
		}

		return weightedList;
	}

	private List<PointWeightPair> getWeightedNeighbors(PointWeightPair pair, Board board, Point treasurePos) {
		Point pos = pair.point;
		int extraWeight = pos.equals(treasurePos) ? TREASUREWALLWEIGHT : 0;
		Openings openings = board.board[pos.y][pos.x].getOpenings();
		int weight = pair.weight;
		List<PointWeightPair> weightedNeighbors = new ArrayList<>();

		// // bonus for fixed cards
		// if (pos.x % 2 == 0 && pos.y % 2 == 0 && weight > FIXEDCARDBONUS) {
		// weight -= FIXEDCARDBONUS;
		// }
		// // malus for position next to treasure
		// if (Math.abs(pos.x - treasurePos.x) + Math.abs(pos.y - treasurePos.y)
		// <= 1) {
		// weight += NEXTTOTREASUREWEIGHT;
		// }

		if (pos.y - 1 >= 0) {
			weightedNeighbors.add(new PointWeightPair(new Point(pos.x, pos.y - 1),
					weight + (openings.isTop() ? SPACEWEIGHT : WALLWEIGHT + extraWeight)
							+ (board.board[pos.y - 1][pos.x].getOpenings().isBottom() ? SPACEWEIGHT
									: WALLWEIGHT + (pos.y - 1 == treasurePos.y && pos.x == treasurePos.x
											? TREASUREWALLWEIGHT : 0))));
		}
		if (pos.x + 1 < 7) {
			weightedNeighbors.add(new PointWeightPair(new Point(pos.x + 1, pos.y),
					weight + (openings.isRight() ? SPACEWEIGHT : WALLWEIGHT + extraWeight)
							+ (board.board[pos.y][pos.x + 1].getOpenings().isLeft() ? SPACEWEIGHT
									: WALLWEIGHT + (pos.y == treasurePos.y && pos.x + 1 == treasurePos.x
											? TREASUREWALLWEIGHT : 0))));
		}
		if (pos.y + 1 < 7) {
			weightedNeighbors.add(new PointWeightPair(new Point(pos.x, pos.y + 1),
					weight + (openings.isBottom() ? SPACEWEIGHT : WALLWEIGHT + extraWeight)
							+ (board.board[pos.y + 1][pos.x].getOpenings().isTop() ? SPACEWEIGHT
									: WALLWEIGHT + (pos.y + 1 == treasurePos.y && pos.x == treasurePos.x
											? TREASUREWALLWEIGHT : 0))));
		}
		if (pos.x - 1 >= 0) {
			weightedNeighbors.add(new PointWeightPair(new Point(pos.x - 1, pos.y),
					weight + (openings.isLeft() ? SPACEWEIGHT : WALLWEIGHT + extraWeight)
							+ (board.board[pos.y][pos.x - 1].getOpenings().isRight() ? SPACEWEIGHT
									: WALLWEIGHT + (pos.y == treasurePos.y && pos.x - 1 == treasurePos.x
											? TREASUREWALLWEIGHT : 0))));
		}
		return weightedNeighbors;
	}

	private List<Point> getNeighbors(Point pos, Board board) {
		List<Point> neighbors = new ArrayList<>();
		Feld feld = board.board[pos.y][pos.x];
		Openings openings = feld.getOpenings();
		if (openings.isTop() && pos.y - 1 >= 0 && board.board[pos.y - 1][pos.x].getOpenings().isBottom()) {
			neighbors.add(new Point(pos.x, pos.y - 1));
		}
		if (openings.isRight() && pos.x + 1 <= 6 && board.board[pos.y][pos.x + 1].getOpenings().isLeft()) {
			neighbors.add(new Point(pos.x + 1, pos.y));
		}
		if (openings.isBottom() && pos.y + 1 <= 6 && board.board[pos.y + 1][pos.x].getOpenings().isTop()) {
			neighbors.add(new Point(pos.x, pos.y + 1));
		}
		if (openings.isLeft() && pos.x - 1 >= 0 && board.board[pos.y][pos.x - 1].getOpenings().isRight()) {
			neighbors.add(new Point(pos.x - 1, pos.y));
		}
		return neighbors;
	}

	public abstract void move();

}
