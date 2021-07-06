package structures.basic.sunit;
import structures.GameState;
import structures.basic.Position;
import structures.basic.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 *  Ranged: Can
 * attack any enemy
 * on the board
 */
public class Pyromancer extends Unit{

    @Override
    public List<Position> attackMark(GameState gameState) {
        List<Position> positions = new ArrayList<>();
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                judgePath(x,y,gameState,false);
            }
        }
        return positions;
    }
}
