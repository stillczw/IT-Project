package structures.basic.sunit;
import structures.GameState;
import structures.basic.Unit;
/**
 * If the enemy player
 * casts a spell, this
 * minion gains +1
 * attack and +1 health
 */
public class PurebladeEnforcer  extends Unit{

    @Override
    public void whenUseSkill(GameState gameState) {
        super.whenUseSkill(gameState);
        if(gameState.currentPlayerId != this.playId){
            this.upAttack(gameState,1);
            cure(gameState,1);
        }
    }
}
