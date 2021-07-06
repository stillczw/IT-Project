package structures.basic.sunit;
import structures.GameState;
import structures.basic.Unit;

/**
 * When this unit is
 * summoned, both
 * players draw a card
 */
public class BlazeHound extends Unit  {

    @Override
    public void whenCreate(GameState gameState) {
        // 摸卡
        gameState.getCard(1,gameState.players[0]);
        gameState.getCard(1,gameState.players[1]);
    }
}
