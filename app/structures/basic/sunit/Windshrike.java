package structures.basic.sunit;
import structures.GameState;
import structures.basic.Position;
import structures.basic.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * Flying: Can move
 * anywhere on the
 * board
 * • When this unit dies,
 * its owner draws a
 * card
 */
public class Windshrike extends Unit{

    @Override
    public List<Position> runMark(GameState gameState) {
        List<Position> positions = new ArrayList<>();
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                judgePath(x,y,gameState,true);
                System.out.println("我的攻击方法被增强了,我可以攻击到任何地方");
                var position = judgePath(x,y,gameState,true);
                if (position!=null) positions.add(position);
            }
        }
		return positions;
    }

    @Override
    public void whenDeath(GameState gameState) {
        //todo 抽卡
        gameState.getCard(1);
    }
}
