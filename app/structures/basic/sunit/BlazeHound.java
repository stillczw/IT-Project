package structures.basic.sunit;
import structures.GameState;
import structures.basic.Unit;

/**
 * • When this unit is summoned, each player draws 1 card
 */
public class BlazeHound extends Unit  {

    @Override
    public void whenCreate(GameState gameState) {
        // draw card from the deck
        gameState.getCard(1,gameState.players[0]);
        gameState.getCard(1,gameState.players[1]);
    }
}
