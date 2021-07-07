package structures.basic.sunit;
import structures.GameState;
import structures.basic.Unit;

/**
 * BattleCry:
 * When this unit is summoned, give your avatar +3 health
 * (maximum 20)
 */
public class AzureHerald extends Unit {

    @Override
    public void whenCreate(GameState gameState) {
        int health = gameState.getCurrentPlayer().getHealth();
        health = health > 17 ? 20 : health + 3;
        gameState.setUserHealth(health);
    }

}
