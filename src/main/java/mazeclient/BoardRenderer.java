package mazeclient;

import mazeclient.generated.BoardType;
import mazeclient.generated.CardType;
import mazeclient.generated.TreasureType;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by mbenndorf on 22.06.2017.
 */
public class BoardRenderer {

	private static final String[] CARD_TEMPLATE = {//
			"X1111Y",//
			"4SSS 2",//
			"4PPP 2",//
			"4xxxx2",//
			"█3333█"//
	};

	public void render(CardType shiftCard, BoardType boardType) {
		System.out.println("======= RENDER ========");
		System.out.println("SHIFT CARD:");
		for (String s : getCard(shiftCard.getOpenings(), shiftCard.getTreasure(), shiftCard.getPin(), -1, -1)) {
			System.out.println(s);
		}
		System.out.println("BOARD:");
		for (int x = 0; x < 7; x++) {
			BoardType.Row row = boardType.getRow().get(x);
			String[] renderedRow = new String[] { "", "", "", "", "" };
			for (int y = 0; y < 7; y++) {
				CardType cardType = row.getCol().get(y);

				combine(renderedRow, getCard(cardType.getOpenings(), cardType.getTreasure(), cardType.getPin(), x, y));
			}

			Arrays.stream(renderedRow).forEach(System.out::println);
		}
		System.out.println("======= ====== ========");
	}

	private void combine(String[] oldRow, String[] newCard) {
		for (int i = 0; i < oldRow.length; i++) {
			oldRow[i] += newCard[i];
		}
	}

	private String[] getCard(CardType.Openings openings, TreasureType treasure, CardType.Pin pin, int x, int y) {
		String[] card = Arrays.copyOf(CARD_TEMPLATE, CARD_TEMPLATE.length);
		String[] replacements = new String[5];
		if (openings.isTop()) {
			replacements[1] = " ";
		}
		if (openings.isRight()) {
			replacements[2] = " ";
		}
		if (openings.isBottom()) {
			replacements[3] = " ";
		}
		if (openings.isLeft()) {
			replacements[4] = " ";
		}

		for (int i = 0; i < card.length; i++) {
			String line = card[i];
			// openings
			for (int j = 1; j < 5; j++) {
				line = line.replace(j + "", replacements[j] == null ? "█" : " ");
			}

			// symbol
			if (treasure != null) {
				line = line.replace("SSS", "S" + treasure.value().replace("sym", ""));
				line = line.replace("SStart", "s");
			} else {
				line = line.replace("SSS", "   ");
			}

			// pins
			String pins = pin.getPlayerID().stream().map(a -> a + "").collect(Collectors.joining(""));
			while (pins.length() < 3) {
				pins += " ";
			}
			line = line.replace("PPP", pins);

			// coords
			line = line.replace("X", x == -1 ? "█" : x + "");
			line = line.replace("Y", y == -1 ? "█" : y + "");

			// empty
			line = line.replace("xxxx", "    ");

			card[i] = line;
		}

		return card;
	}
}
