package structures.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import commands.BasicCommands;
import structures.GameState;
import utils.BasicObjectBuilders;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a representation of a Unit on the game board.
 * A unit has a unique id (this is used by the front-end.
 * Each unit has a current UnitAnimationType, e.g. move,
 * or attack. The position is the physical position on the
 * board. UnitAnimationSet contains the underlying information
 * about the animation frames, while ImageCorrection has
 * information for centering the unit on the tile. 
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Unit implements SkillListenable{

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java objects from a file
	protected UnitAnimationType animation;
	protected Position position;
	protected UnitAnimationSet animations;
	protected ImageCorrection correction;


	protected int id;
	protected int playId = 1;
	protected boolean isPlayerUnit = false;

	protected boolean provoke = false;

	boolean firstOut = true;
	public int runCount = 0;
	protected int attackCount = 0;
	protected boolean actioned = false;

	// 每回合开始前被调用
	public void init(){
		runCount = 1;
		attackCount = 1;
		actioned = false;
	}

	public void initialize(Card card,GameState gameState){
		if(card != null){
			this.setAttack(card.getBigCard().getAttack());
			this.setHealth(card.getBigCard().getHealth());
			setHealthMax(health);
		}else {
			setHealthMax(20);
			setHealth(20);
			setAttack(1);
		}
		gameState.sleep(30);
		BasicCommands.setUnitHealth(gameState.out,this,this.getHealth());
		gameState.sleep(30);
		BasicCommands.setUnitAttack(gameState.out,this,this.getAttack());
	}

	// todo 是否已行动过
	public boolean actioned(){
		if(firstOut){
			return firstOut;
		}else{
			if(actioned) return actioned;
			if(!canAttack()&&!canRun())
				actioned=true;
			return actioned;
		}
	}

	public void actionMark(GameState gameState){
		int tx = getPosition().getTilex();
		int ty = getPosition().getTiley();
		System.out.println("判断 "+tx+","+ty+" 上点的可行路径");
		attackFind(gameState);
		runFind(gameState);
	}
	private Unit isProvoked(GameState gameState){
		int tx = getPosition().getTilex();
		int ty = getPosition().getTiley();
		System.out.println("判断 "+tx+","+ty+" 上点的可行路径");

		int[][] ps = { {tx-1,ty},{tx+1,ty},{tx,ty-1},{tx,ty+1}, };
		for (int[] p : ps) {
			if(p[0]<0||p[0]>8||p[1]<0||p[1]>4) {
				continue;
			}
			if(gameState.units[p[0]][p[1]]!=null)
				// 当前位置有嘲讽怪，且为敌人
			if(gameState.units[p[0]][p[1]].playId != playId &&gameState.units[p[0]][p[1]].provoke) {
				return gameState.units[p[0]][p[1]];
			}
		}
		return null;
	}

	public void run(GameState state,Tile tile){
		int x = getPosition().getTilex();
		int y = getPosition().getTiley();
		//units集合更新
		state.units[x][y] = null;
		state.units[tile.getTilex()][tile.getTiley()] = this;
		setPositionByTile(tile);
		runCount--;
		//绘画事件发布
		BasicCommands.moveUnitToTile(state.out,this,tile);
		state.sleep(1000);

	}

	public List<Position> attackFind(GameState gameState){
		if(!canAttack()) {
			gameState.aiSay("该单元不能攻击");
			return GameState.nullList;
		}
		var unit = isProvoked(gameState);
		// 如果嘲讽单元不为空，强制攻击范围为该单元
		if(unit!=null) {
			gameState.redLight(BasicObjectBuilders.loadTile(unit.getPosition().getTilex(),unit.getPosition().getTiley()));
			ArrayList<Position> positions = new ArrayList<>();
			positions.add(new Position(unit.getPosition().getTilex(),unit.getPosition().getTiley()));
			return positions;
		}
		return attackMark(gameState);
	}
	public List<Position> runFind(GameState gameState){
		if(isProvoked(gameState)!=null) return GameState.nullList;
		if(canRun())
			return runMark(gameState);
		else return GameState.nullList;
	}

	public List<Position> attackMark(GameState gameState){
		List<Position> positions = new ArrayList<>();
		int tx = getPosition().getTilex();
		int ty = getPosition().getTiley();
		int[][] ps = { {tx-1,ty},{tx+1,ty},{tx,ty-1},{tx,ty+1},{tx-1,ty-1},{tx+1,ty-1},{tx+1,ty+1},{tx-1,ty+1}, };
		for (int[] p : ps) {
			var position = judgePath(p[0],p[1],gameState,false);
			if (position!=null) positions.add(position);
		}
		return positions;
	}

	public List<Position> runMark(GameState gameState){
		List<Position> positions = new ArrayList<>();
		int tx = getPosition().getTilex();
		int ty = getPosition().getTiley();
		int[][][] ps = {
					{{tx-1,ty},{tx-2,ty}},
					{{tx + 1, ty},{tx+2,ty}},
					{{tx, ty - 1},{tx,ty-2}},
					{{tx, ty + 1},{tx,ty+2}},
				{{tx - 1, ty - 1}}, {{tx + 1, ty - 1}}, {{tx - 1, ty + 1}}, {{tx + 1, ty + 1}},};
		for (int[][] p : ps) {
			var position = judgePath(p,gameState,true);
			if (position!=null) positions.addAll(position);
		}
		return positions;
	}
	protected Position judgePath(int tx,int ty,GameState gameState,boolean runMethod){
		if(tx<0||tx>8||ty<0||ty>4) {
			System.out.println(tx+","+ty+" 不可行");
			return null;
		}
		else {
			Tile tile = BasicObjectBuilders.loadTile(tx, ty);
			Unit unit = gameState.units[tx][ty];
			if(unit!=null){
				// 如果是敌方单位，标红
				if(!runMethod && unit.getPlayId()!=gameState.currentPlayerId){
					gameState.redLight(tile);
					return new Position(tx,ty);
				}
			}else if(runMethod){
				gameState.highLight(tile);
				return new Position(tx,ty);
			}
		}
		return null;
	}

	protected List<Position> judgePath(int[][] pos, GameState gameState, boolean runMethod){
		List<Position> positions = new ArrayList<>();
		for (int[] p : pos) {
			int tx = p[0]; int ty = p[1];
			if(tx<0||tx>8||ty<0||ty>4) {
				System.out.println(tx+","+ty+" 不可行");
				break;
			}else {
				Tile tile = BasicObjectBuilders.loadTile(tx, ty);
				Unit unit = gameState.units[tx][ty];
				if(unit!=null){
					// 如果是敌方单位，标红
					if(!runMethod && unit.getPlayId()!=gameState.currentPlayerId){
						gameState.redLight(tile);
						positions.add( new Position(tx,ty));
						break;
					}
				}else if(runMethod){
					gameState.highLight(tile);
					positions.add( new Position(tx,ty));
				}
			}
		}
		return positions;
	}

	public boolean canAttack(){
		return attackCount>0;
	}

	public boolean canRun(){
		return runCount>0;
	}


	public boolean isPlayerUnit() {
		return isPlayerUnit;
	}
	public void setPlayerUnit() {
		this.health = 20;
		this.attack = 0;
		isPlayerUnit = true;
	}
	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	int healthMax ;
	int health ;
	int attack ;

	public int getHealthMax() {
		return healthMax;
	}

	public void setHealthMax(int healthMax) {
		this.healthMax = healthMax;
	}

	public Unit() {}

	public Unit(int id, int playId,UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.playId = playId;
		this.animation = UnitAnimationType.idle;
		position = new Position(currentTile.getXpos(),currentTile.getYpos(),currentTile.getTilex(),currentTile.getTiley());
		this.correction = correction;
		this.animations = animations;
	}

	public void setPlayId(int playId) {
		this.playId = playId;
	}
	public int getPlayId() {
		return playId;
	}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		
		position = new Position(0,0,0,0);
		this.correction = correction;
		this.animations = animations;
	}
	
	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		
		position = new Position(currentTile.getXpos(),currentTile.getYpos(),currentTile.getTilex(),currentTile.getTiley());
		this.correction = correction;
		this.animations = animations;
	}
	
	
	
	public Unit(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations,
			ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = animation;
		this.position = position;
		this.animations = animations;
		this.correction = correction;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public UnitAnimationType getAnimation() {
		return animation;
	}
	public void setAnimation(UnitAnimationType animation) {
		this.animation = animation;
	}

	public ImageCorrection getCorrection() {
		return correction;
	}

	public void setCorrection(ImageCorrection correction) {
		this.correction = correction;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public UnitAnimationSet getAnimations() {
		return animations;
	}

	public void setAnimations(UnitAnimationSet animations) {
		this.animations = animations;
	}
	
	/**
	 * This command sets the position of the Unit to a specified
	 * tile.
	 * @param tile
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(),tile.getYpos(),tile.getTilex(),tile.getTiley());
	}

	public int attack(GameState gameState,Unit unit){
		System.out.println("unit发动攻击，攻击力为 "+ attack);
		//绘画事件发布
		gameState.resetClickType();
		BasicCommands.playUnitAnimation(gameState.out,this,UnitAnimationType.attack);
		gameState.sleep(400);
		unit.hit(gameState,attack);
		attackCount--;
		if(attackCount<=0) runCount=0;
		return health;
	}

	public int hit(GameState gameState,int v){
		System.out.println("unit受到伤害，原血量"+health+"攻击力为"+v+" 剩余血量 "+ (this.health-v));
		this.health=this.health-v;
		BasicCommands.playUnitAnimation(gameState.out,this,UnitAnimationType.hit);
		gameState.sleep(100);
		BasicCommands.setUnitHealth(gameState.out,this,this.health);
		// 对于Avatar的特别效果
		if(isPlayerUnit){
			gameState.aiSay(" player "+getPlayId()+" 受到伤害");
			gameState.players[getPlayId()].setHealth(this.health);
			BasicCommands.setPlayerHealth(gameState.out,getPlayId()+1,gameState.players[getPlayId()]);
			gameState.sleep(30);
			// 执行avatar受伤回调
			for (Unit[] us : gameState.units) {
				for (Unit u : us) {
					if(u!=null)
						u.whenAvatarHit(gameState);
				}
			}
			if(health<=0)
				gameState.gameEnd(this.playId);
		}
		if(health<=0){
			gameState.removeUnit(this);
		}
		return this.health;
	}

	public int cure(GameState gameState,int v){
		health+=v;
		health = Math.min(health, healthMax);
		System.out.println("unit获得治疗，剩余血量 "+ health);
		gameState.sleep(30);
		BasicCommands.setUnitHealth(gameState.out,this,this.getHealth());
		gameState.sleep(30);
		return health;
	}

	public int upAttack(GameState gameState,int v){
		attack+=v;
		System.out.println("unit获得攻击力增益，当前攻击力为 "+ attack);
		gameState.sleep(30);
		BasicCommands.setUnitAttack(gameState.out,this,this.getAttack());
		gameState.sleep(30);
		return attack;
	}

	public Unit loadByUnit(Unit unit){
		this.id = unit.id;
		this.playId = unit.playId;
		this.animation = unit.animation;
		this.position = unit.position;
		this.animations = unit.animations;
		this.correction = unit.correction;
		this.health = unit.health;
		this.attack = unit.attack;
		this.isPlayerUnit = unit.isPlayerUnit;
		return this;
	}

	@Override
	public void whenCreate(GameState gameState) {

	}

	@Override
	public void whenDeath(GameState gameState) {

	}

	@Override
	public void whenAvatarHit(GameState gameState) {

	}

	@Override
	public void whenUseSkill(GameState gameState) {

	}

	@Override
	public void useSkill(GameState gameState, Unit unit) {

	}

	@Override
	public String toString() {
		return "Unit{" +
				"id=" + id +
				", playId=" + playId +
				", isPlayerUnit=" + isPlayerUnit +
				", provoke=" + provoke +
				", firstOut=" + firstOut +
				", runCount=" + runCount +
				", attackCount=" + attackCount +
				", actioned=" + actioned +
				", healthMax=" + healthMax +
				", health=" + health +
				", attack=" + attack +
				'}';
	}
}
