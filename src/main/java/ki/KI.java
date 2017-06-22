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

	Point getPositionOfTreasure() {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				if (data.board.getFeld(j, i).getTreasure() == data.treasure) {
					return new Point(j, i);
				}
			}
		}
		return null;
	}

	public KI(KiData data, MazeClient client) {
		this.data = data;
		this.client = client;
	}

	public boolean isCardIShape(CardType card) {
		Openings openings = card.getOpenings();
		return (openings.isTop() && openings.isBottom() && !openings.isLeft() && !openings.isRight()) || (
				!openings.isTop() && !openings.isBottom() && openings.isLeft() && openings.isRight());
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
		newCard.setOpenings(card.getOpenings());
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
