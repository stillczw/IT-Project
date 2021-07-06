package structures.basic.sunit;
import structures.GameState;
import structures.basic.Unit;

/**
 * When this unit is
 * summoned give your
 * avatar +3 health
 * (maximum 20)
 *
 */
public class AzureHerald extends Unit {

    @Override
    public void whenCreate(GameState gameState) {
        // todo 在这里执行加血逻辑
        int playerHealth = gameState.getCurrentPlayer().getHealth();
        playerHealth = playerHealth>17?20:playerHealth+3;
        gameState.setUserHealth(playerHealth);
    }

}
