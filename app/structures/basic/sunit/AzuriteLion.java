package structures.basic.sunit;
import structures.GameState;
import structures.basic.Unit;

/**
 * Windfury:
 * Attack twice per turn
 */
public class AzuriteLion extends Unit {

    @Override
    public void init() {
        super.init();
        this.attackCount = 2;

    }

    @Override
    public int attack(GameState gameState, Unit unit) {
        int i = super.attack(gameState, unit);
        if(attackCount > 0) runCount = 1;
        return i;
    }
}

