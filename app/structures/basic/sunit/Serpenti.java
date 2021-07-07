package structures.basic.sunit;
import structures.GameState;
import structures.basic.Unit;

/**
 * Windfury:
 * Attack twice per turn
 */
public class Serpenti extends Unit{

    @Override
    public void init(){
        runCount = 1;
        attackCount = 2;
        actioned = false;
    }
}
