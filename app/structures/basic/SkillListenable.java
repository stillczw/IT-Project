package structures.basic;

import structures.GameState;

public interface SkillListenable {

    void whenCreate(GameState gameState);
    void whenDeath(GameState gameState);
    void whenAvatarHit(GameState gameState);
    void whenUseSkill(GameState gameState);

    /**
     *
     * @param gameState
     * @param unit 可能存在的敌对unit
     */
    void useSkill(GameState gameState,Unit unit);
}
