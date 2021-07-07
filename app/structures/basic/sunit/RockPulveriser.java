package structures.basic.sunit;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Unit;

/**
 * Provoke:
 * If an enemy unit can attack and is adjacent to any unit
 * with provoke, then it can only choose to attack the provoke units.
 * Enemy units cannot move when provoked
 */
public class RockPulveriser extends Unit{

    @Override
    public void initialize(Card card, GameState gameState) {
        super.initialize(card, gameState);
        this.provoke = true;
    }
}
