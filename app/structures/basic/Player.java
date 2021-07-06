package structures.basic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * A basic representation of of the Player. A player
 * has health and mana.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Player {

	int health;
	int mana;
	public Unit unit;

	final Queue<String> deckQueue = new ArrayDeque<>();
	List<Card> handCards = new ArrayList<>();

	public Player() {
		super();
		this.health = 20;
		this.mana = 0;
	}

	public Player(int health, int mana) {
		super();
		this.health = health;
		this.mana = mana;
	}

	public Queue<String> getDeckQueue() {
		return deckQueue;
	}

	public List<Card> getHandCards() {
		return handCards;
	}

	public String peek(){
		return getDeckQueue().peek();
	}

	public int getHealth() {
		return health;
	}
	public void setHealth(int health) {
		this.health = health;
	}
	public int getMana() {
		return mana;
	}
	public void setMana(int mana) {
		this.mana = mana;
	}
	
	
	
}
