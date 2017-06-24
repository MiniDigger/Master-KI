package ki;

import mazeclient.generated.CardType;

public class Feld extends CardType {
	private boolean treasureIsCollected;
	
	public Feld(CardType cardType) {
		setOpenings(cardType.getOpenings());
		setPin(cardType.getPin());
		setTreasure(cardType.getTreasure());
		treasureIsCollected = false;
	}

	public boolean isTreasureIsCollected() {
		return treasureIsCollected;
	}

	public void setTreasureIsCollected(boolean treasureIsCollected) {
		this.treasureIsCollected = treasureIsCollected;
	}

}
