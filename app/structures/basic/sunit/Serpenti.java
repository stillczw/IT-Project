package structures.basic.sunit;
import structures.GameState;
import structures.basic.Unit;

/**
 * Can attack twice per
 * turn
 */
public class Serpenti extends Unit{
    @Override
    public void init(){
        runCount = 1;
        attackCount = 2;
        actioned = false;
    }
}
