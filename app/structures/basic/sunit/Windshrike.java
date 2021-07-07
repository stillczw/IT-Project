package structures.basic.sunit;
import structures.GameState;
import structures.basic.Position;
import structures.basic.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * • When this unit dies, its owner draws a card
 * Flying:
 * Can move anywhere on the board
 */
public class Windshrike extends Unit{

    @Override
    public List<Position> runMark(GameState gameState) {
        List<Position> positions = new ArrayList<>();
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                judgePath(x,y,gameState,true);
                System.out.println("I can move anywhere on the board");
                var position = judgePath(x,y,gameState,true);
                if (position!=null) positions.add(position);
            }
        }
		return positions;
    }

    @Override
    public void whenDeath(GameState gameState) {
        // draw card from the deck
        gameState.getCard(1);
    }
}
