package structures;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import org.apache.commons.lang3.ArrayUtils;
import structures.basic.*;
import structures.basic.sunit.*;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.*;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState {

	public Unit[][] units = new Unit[9][5];
	public ActorRef out = null;
	TileType[][] tileTypes = new TileType[9][5];

	final static int player_id1 = 0;
	final static int player_id2 = 1;
	static int id = 1;
	static int currentRound = 0;
	static boolean end = false;

	public final static List<Position> nullList = new ArrayList<>();

	// 当前执行动作的用户ID
	public int currentPlayerId = -1;

	public ClickType clickType = ClickType.none;


	int clickPosition = -1;

	public void setUserHealth(int health){
		getCurrentPlayer().setHealth(health);
		playerAvatars[currentPlayerId].setHealth(health);
		BasicCommands.setUnitHealth(out,playerAvatars[currentPlayerId],health);
		sleep(30);
		BasicCommands.setPlayerHealth(out,currentPlayerId,getCurrentPlayer());
		sleep(30);
	}

	public void setClickPosition(int clickPosition) {
		System.out.println("set clickPosition : "+clickPosition);
		this.clickPosition = clickPosition;
	}
	public void setClickType(ClickType clickType) {
		this.clickType = clickType;
	}

	public Player[] players;
	public Unit[] playerAvatars = new Unit[2];

	final static List<String> deck1Cards = StaticConfFiles.deck1Cards;
	final static List<String> deck2Cards = StaticConfFiles.deck2Cards;

	public void init(ActorRef out){
		this.out = out;

		// 初始化参数
		// setPlayersHealth
		Player humanPlayer = new Player(20, 0);
		BasicCommands.setPlayer1Health(out, humanPlayer);
		BasicCommands.setPlayer1Mana(out, humanPlayer);
		Player aiPlayer = new Player(20, 0);
		BasicCommands.setPlayer2Health(out, aiPlayer);
		BasicCommands.setPlayer2Mana(out, aiPlayer);

		players = new Player[]{humanPlayer, aiPlayer};

		// 下一步是抽卡,游戏开始每个玩家都抽2卡  卡池来自
		//	牌库初始化
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			int index = random.nextInt(deck1Cards.size());
			humanPlayer.getDeckQueue().add(deck1Cards.get(index));
			deck1Cards.remove(index);
			aiPlayer.getDeckQueue().add(deck2Cards.get(index));
			deck2Cards.remove(index);
		}

//		aiSay(" ----------玩家初始回合牌库数量 : "+humanPlayer.getDeckQueue().size());

		// 9x5 tile池
		resetClickType();

		Unit player1 = loadUnit("humanAvatar",player_id1,BasicObjectBuilders.loadTile(0, 2),true);
		Unit player2 = loadUnit("aiAvatar",player_id2,BasicObjectBuilders.loadTile(8, 2),true);
		humanPlayer.unit = player1;aiPlayer.unit=player2;
		playerAvatars = new Unit[]{player1,player2};

		// 玩家抽卡
		turnController();
	}

	public Unit loadUnit(String cardName,int playerId,Tile tile,boolean isPlayer){
		int x = tile.getTilex();int y = tile.getTiley();

		System.out.println("-- 放置卡片 ： "+cardName);
		String config = StaticConfFiles.cardToUnit.get(cardName);
		Unit unit = BasicObjectBuilders.loadUnit(config, id++, playerId, Unit.class);
		System.out.println("从原有方法加载的unit参数  "+unit);
		unit.setPositionByTile(tile);
		units[x][y] = unit;
		sleep(30);
		BasicCommands.drawUnit(out,unit,tile);
		if(isPlayer) {
			unit.setPlayerUnit();
			unit.initialize(null,this);
		}
		return unit;
	}


	public void loadUnit(Card card,int playerId,Tile tile){
		int x = tile.getTilex();int y = tile.getTiley();

		String cardName = card.getCardname().toLowerCase().replace(" ", "_");
		Unit unit = loadUnit(cardName,playerId,tile,false);
		switch (card.getCardname()){
			case "Pureblade Enforcer":
				unit = new PurebladeEnforcer().loadByUnit(unit);
				break;
			case "Azure Herald":
				unit = new AzureHerald().loadByUnit(unit);
				break;
			case "Silverguard Knight":
				unit = new SilverguardKnight().loadByUnit(unit);
				break;
			case "Azurite Lion":
				unit = new AzuriteLion().loadByUnit(unit);
				break;
			case "Fire Spitter":
				unit = new FireSpitter().loadByUnit(unit);
				break;
			case "Ironcliff Guardian":
				unit = new IroncliffGuardian().loadByUnit(unit);
				break;
			case "Planar Scout":
				unit = new PlanarScout().loadByUnit(unit);
				break;
			case "Rock Pulveriser":
				unit = new PurebladeEnforcer().loadByUnit(unit);
				break;
			case "Pyromancer":
				unit = new PurebladeEnforcer().loadByUnit(unit);
				break;
			case "Blaze Hound":
				unit = new PurebladeEnforcer().loadByUnit(unit);
				break;
			case "Windshrike":
				unit = new PurebladeEnforcer().loadByUnit(unit);
				break;
			case "Serpenti":
				unit = new PurebladeEnforcer().loadByUnit(unit);
				break;
		}
		unit.initialize(card,this);
		units[x][y] = unit;
		getCurrentPlayer().unit.runCount = 0;
		unit.whenCreate(this);
	}

	/**
	 * 更新仆从信息并绘制
	 * @param tile 出战位置
	 */
	public void setUnit(Tile tile,Card card) {
		// 计算法力消耗，法力不足无法出战
		if(card.getManacost()>getCurrentPlayer().getMana()){
			BasicCommands.addPlayer1Notification(out,"法力不足",1);
			return;
		}else{
			//法力足够直接扣除
			players[currentPlayerId].setMana(getCurrentPlayer().getMana()-card.getManacost());
			BasicCommands.setPlayerMana(out,currentPlayerId,getCurrentPlayer());
		}
		//绘画事件发布
		loadUnit(card,currentPlayerId,tile);
		removeCard(card);
	}

	/**
	 *
	 * @param num 抽卡的数量
	 */
	public void getCard(int num) {
		getCard(num,getCurrentPlayer());
	}

	public void getCard(int num,Player player) {
		System.out.println(" ----------玩家新回合牌库数量 : "+getCurrentPlayer().getDeckQueue().size());
		for (int pos = 1; pos <=num; pos++) {
			String newCard = player.getDeckQueue().poll();
			if(newCard==null) {
				System.out.println("无法抽卡，卡池中已经没有卡了！");
				return;
			}
			Card card = BasicObjectBuilders.loadCard(newCard, pos, Card.class);
			if(player.getHandCards().size()>=6){
				System.out.println("手牌无法放下更多(>=6)，新卡牌被废弃");
				continue;
			}
			player.getHandCards().add(card);
		}
		if(isUserRound()){
			//用户回合，重绘制卡片
			System.out.println("用户回合，绘制卡片");
			drawCards();
		}
	}

	private void drawCards(){
		for (int i = 0; i < getCurrentPlayer().getHandCards().size(); i++) {
			BasicCommands.drawCard(out, getCurrentPlayer().getHandCards().get(i), i+1, 0);
		}
	}

	/**
	 * 更新手牌数量及图形绘制
	 * @param card 删除的卡
	 */
	private void removeCard(Card card){
		for (int i = getCurrentPlayer().getHandCards().size(); i >0; i--) {
			System.out.println("删除第"+i+"张卡的绘制");
			BasicCommands.deleteCard(out, i);
		}
		//hands更新
		getCurrentPlayer().getHandCards().remove(card);

		if(isUserRound())
			drawCards();
	}


	/**
	 * 高亮 tile ，并更新type组属性
	 */
	public void highLight(Tile tile){
		if(tileTypes[tile.getTilex()][tile.getTiley()] != TileType.highLight){
			BasicCommands.drawTile(out, tile, 1);
			tileTypes[tile.getTilex()][tile.getTiley()] = TileType.highLight;
			System.out.println("set "+tile+" highlight");
		}
	}

	public void redLight(Tile tile){
		if(tileTypes[tile.getTilex()][tile.getTiley()] != TileType.red){
			BasicCommands.drawTile(out, tile, 2);
			tileTypes[tile.getTilex()][tile.getTiley()] = TileType.red;
			System.out.println("set "+tile+" red");
		}
	}

	/**
	 * 瓷砖点击效果
	 */
	public void tileClicked(JsonNode message) {
		if(end){
			System.out.println("游戏已结束");
			aiSay("游戏已结束");
			return;
		}
		int x = message.get("tilex").asInt();
		int y = message.get("tiley").asInt();

//		aiSay("点击瓷砖 "+x+","+y);

		Tile tile = BasicObjectBuilders.loadTile(x, y);
		switch (clickType){
			case card:
				if(tileTypes[x][y]==TileType.highLight){
					//出战 或 发动效果
					var curHandCard = getCurrentPlayer().getHandCards().get(clickPosition-1);
					setUnit(tile,curHandCard);
				}
				// 取消卡牌点击效果
				resetClickType();
				break;
			case none:
				System.out.println("ClickType ： none");
				// 判断该瓷砖是否有仆从
				if (units[x][y]!=null){
					Unit unit = units[x][y];
					System.out.println("瓷砖有[unit] "+unit);
					//判断是否为当前玩家ID
					if(unit.getPlayId()==currentPlayerId){
						//  是否可行走,攻击
						if(unit.actioned()){
							// 绘制可行走路径、攻击路径
							System.out.println("瓷砖上的[unit]是[可操作]的");
							clickType = ClickType.unit;
							setClickPosition(x*10+y);
							unit.actionMark(this);
						}else {
							aiSay("Unit 不能行动");
						}
					}
				}
				break;
			case effectCard:
				var p = clickPosition-1;
				Card card = getCurrentPlayer().getHandCards().get(p);
				doEffect(card,tile);
				break;
			case unit:
				//精灵单元的移动攻击事件
				//  tile是目标位置
				int ox = clickPosition/10;
				int oy = clickPosition%10;

				if(tileTypes[x][y]==TileType.highLight){
					moveUnitToTile(tile);
				}else if(tile.getTilex()==ox&&tile.getTiley()==oy){
					resetClickType();
				}else if(tileTypes[x][y]==TileType.red){
					// 攻击事件
					attack(tile);
					System.out.println("触发攻击事件");
				}
				break;
			case tile:
				break;

		}
	}

	private void doEffect(Card card,Tile tile) {
		if(card.getManacost()>getCurrentPlayer().getMana()){
			aiSay("法力值不足以发动技能");
			resetClickType();
			return;
		}
		int ox = tile.getTilex();
		int oy = tile.getTiley();
		TileType tileType = tileTypes[ox][oy];
		if(tileType==TileType.normal) {
			System.out.println("点到了错误的砖块["+ox+","+oy+"]");
		}
		Unit unit = units[ox][oy];
		System.out.println("执行技能牌 ： "+card.getCardname());
		switch (card.getCardname()){
			case "Truestrike":
				//  Deal 2 damage to an enemy unit
				if(tileType==TileType.red)
					unit.hit(this,2);
				break;
			case "Entropic Decay":
				// Reduce a non-avatar unit to 0 health
				if(tileType==TileType.red)
					removeUnit(unit);
				break;
			case "Sundrop Elixir":
				// Add +5 health to a Unit.
				if(tileType==TileType.highLight)
					unit.cure(this,5);
				break;
			case "Staff of Y’Kir":
				// Add +2 attack to your avatar
				if(tileType==TileType.highLight)
					unit.upAttack(this,2);
				break;
		}
		getCurrentPlayer().setMana(getCurrentPlayer().getMana()-card.getManacost());
		BasicCommands.setPlayerMana(out,currentPlayerId,getCurrentPlayer());
		sleep(30);
		removeCard(card);
		for (Unit[] us : units) {
			for (Unit u : us) {
				if(u!=null)
				u.whenUseSkill(this);
			}
		}
		resetClickType();
	}

	private String formatCardName(String cardname) {
		return cardname.toLowerCase().replace(" ", "_");
	}

	private void attack(Tile tile) {
		//合法值判断
		if(clickPosition<1) {
			System.out.println("非法攻击位置");
			return;
		}
		int x = clickPosition/10;
		int y = clickPosition%10;
		System.out.println(x+","+y+" -> "+tile.getTilex()+","+tile.getTiley());
		var unit = units[x][y];
		var unit2 = units[tile.getTilex()][tile.getTiley()];
		unit.attack(this,unit2);
	}

	public void sleep(int ms){
		try {Thread.sleep(ms);} catch (InterruptedException e) {e.printStackTrace();}
	}

	/**
	 * unit死亡方法
	 */
	public void removeUnit(Unit unit){
		sleep(30);
		System.out.println("unit 死亡 , 触发死亡事件");
		BasicCommands.playUnitAnimation(out,unit,UnitAnimationType.death);
		units[unit.getPosition().getTilex()][unit.getPosition().getTiley()]=null;
		sleep(1000);
		BasicCommands.deleteUnit(out,unit);
		sleep(30);
		unit.whenDeath(this);
	}

	private void moveUnitToTile(Tile tile) {

		//合法值判断
		if(clickPosition<1) return;
		int x = clickPosition/10;
		int y = clickPosition%10;
		System.out.println(x+","+y+" -> "+tile.getTilex()+","+tile.getTiley());
		var unit = units[x][y];
		resetClickType();
		unit.run(this,tile);
	}


	public void resetClickType(){
		sleep(30);
		clickType = ClickType.none;
		setClickPosition(-1);
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				if(tileTypes[i][j]!=TileType.normal){
					tileTypes[i][j]=TileType.normal;
					Tile tile = BasicObjectBuilders.loadTile(i, j);
					BasicCommands.drawTile(out, tile, 0);
				}
			}
		}
		sleep(30);
	}

	/**
	 * 是否为玩家回合
	 */
	boolean isUserRound(){
		return currentPlayerId==player_id1;
	}

	public void cardClicked(JsonNode message) {
		if(end){
			System.out.println("游戏已结束");
			aiSay("游戏已结束");
			return;
		}
		if(!isUserRound()) return;
		if(clickType == ClickType.card){
			resetClickType();
			BasicCommands.addPlayer1Notification(out,"取消卡选择",1);
			return;
		}
		int handPosition = message.get("position").asInt();
//		  放置卡牌
		//判断卡牌类型 技能卡或人物卡
		var card = getCurrentPlayer().getHandCards().get(handPosition-1);
		if(StaticConfFiles.cardToUnit.get(formatCardName(card.getCardname()))!=null){
			// 是人物卡
			if (clickType != ClickType.card) {
				resetClickType();
				setClickType(ClickType.card);
				setClickPosition(handPosition);

				// todo fixed
				if(card.getCardname().equals("Planar Scout") || card.getCardname().equals("Ironcliff Guardian")){
					for (int i = 0; i < 9; i++) {
						for (int j = 0; j < 5; j++) {
							if (units[i][j] != null) continue;
							Tile tile = BasicObjectBuilders.loadTile(i, j);
							highLight(tile);
						}
					}
				}else {
					for (int i = 0; i < 9; i++) {
						for (int j = 0; j < 5; j++) {
							if (units[i][j] != null) {
								if (units[i][j].getPlayId() == this.currentPlayerId) {
									for (int k = i < 1 ? i : i - 1; k <= (i == 8 ? i : i + 1); k++) {
										for (int l = j < 1 ? j : j - 1; l <= (j == 4 ? j : j + 1); l++) {
											if (((k != i) || (l != j)) && units[k][l] == null) {
												Tile tile = BasicObjectBuilders.loadTile(k, l);
												highLight(tile);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}else{
			//  是技能卡
			if (clickType != ClickType.effectCard) {
				resetClickType();
				setClickType(ClickType.effectCard);
				setClickPosition(handPosition);

				clickEffectCard(card);


			}
		}
	}

	private List<Tile> clickEffectCard(Card card) {
		List<Tile> tiles = new ArrayList<>();
		BasicCommands.addPlayer1Notification(out,"选中了一个技能卡",1);
		switch (card.getCardname()){
			case "Truestrike":
				//  Deal 2 damage to an enemy unit
				for (int i = 0; i < 9; i++) {
					for (int j = 0; j < 5; j++) {
						if (units[i][j] == null) continue;
						if (units[i][j].getPlayId() != currentPlayerId) {
							Tile tile = BasicObjectBuilders.loadTile(i, j);
							tiles.add(tile);
							redLight(tile);
						}
					}
				}
				break;
			case "Sundrop Elixir":
			case "Staff of Y’Kir'":
				// 1 Add +5 health to a Unit.
				//	   2 Add +2 attack to your avatar
				for (int i = 0; i < 9; i++) {
					for (int j = 0; j < 5; j++) {
						if (units[i][j] == null) continue;
						if (units[i][j].getPlayId() == currentPlayerId) {
							Tile tile = BasicObjectBuilders.loadTile(i, j);
							tiles.add(tile);
							highLight(tile);
						}
					}
				}
				break;
			case "Entropic Decay":
				// Reduce a non-avatar unit to 0 health
				for (int i = 0; i < 9; i++) {
					for (int j = 0; j < 5; j++) {
						if (units[i][j] == null) continue;
						if (!units[i][j].isPlayerUnit() && units[i][j].getPlayId() != currentPlayerId) {
							Tile tile = BasicObjectBuilders.loadTile(i, j);
							tiles.add(tile);
							redLight(tile);
						}
					}
				}
				break;
		}
		return tiles;
	}

	public Player getCurrentPlayer(){
		return players[currentPlayerId];
	}

	/**
	 *  在该方法转换控制权
	 */
	void turnController(){
		// 清除状态
		resetClickType();
		// 当前玩家标记轮转
		currentPlayerId = (currentPlayerId+1)%2;
		//  固定玩家先走的情况下，轮到玩家 意味着 新回合开始 标记+1
		if(currentPlayerId==0) {
			currentRound+=1;
		}
		// 第一回合的特殊值
		if(currentRound==1)
			getCard(3);
		else getCard(1);

		// 玩家场上单元回合初始化
		for (Unit[] unitArr : units) {
			for (Unit unit : unitArr) {
				if(unit!=null)
				if(unit.getPlayId() == currentPlayerId)
					unit.init();
			}
		}

		var cPlayer = getCurrentPlayer();
		cPlayer.setMana(currentRound>8?9:currentRound+1);
		BasicCommands.setPlayerMana(out,currentPlayerId,cPlayer);

		if(currentPlayerId==1) {
			AiMethod();
		}
	}

	public void aiSay(String str){
		System.out.println(str);
		BasicCommands.addPlayer1Notification(out,str,1);
		sleep(500);
	}

	public void otherClick(){
		resetClickType();
	}

	//在这里执行 AI 逻辑
	private void AiMethod() {
		Player player = players[1];
		// 首先处理手牌   对于人物牌，没有选择倾向，从第一张牌开始释放
		var hands = player.getHandCards();
		int index = 0;

		aiSay("AI出牌阶段开始");
		while (player.getMana()>0 && hands.size()>0 && hands.size()>index){


			Card card = hands.get(index);
			if (card.getManacost()>player.getMana()) {
//				aiSay("卡"+index+"需求["+card.getManacost()+"]，跳过");
				index++;
			}else{
				if(StaticConfFiles.cardToUnit.get(card.getFormatName())==null){
					// 技能牌操作
					aiSay("展示技能牌范围 index -> "+index+" cardName -> "+card.getFormatName());
					List<Tile> tiles = clickEffectCard(card);
					sleep(1000);
					if(tiles.size()>0){
						doEffect(card,tiles.get(0));
						sleep(200);
					}
					else {
						aiSay("该技能无法作用，跳过");
						index++;
					}
				}else{
					// 考虑出卡位置，倾向于由左到右,由上至下放置;
					out:for (int x = 6; x < 9; x++) {
						for (int y = 0; y < 5; y++) {
							if(units[x][y]==null){
								aiSay("卡牌出战 ["+x+","+y+"]");
								setUnit(BasicObjectBuilders.loadTile(x, y),card);
								break out;
							}
						}
					}
					// 如果可放置位置已满，跳过手牌释放
					break;
				}
			}
		}
		aiSay("AI出牌阶段结束，战斗阶段开始");
		//  按轮次检索所属单位，并依次控制。 检索倾向为，从左到右，从上到下
		for (Unit[] us : units) {
			for (Unit u : us) {
				if(u!=null)
				if(u.getPlayId() == currentPlayerId){
//					aiSay("展示所属单位移动攻击范围");
					var attacked = false;
					for (int i = 0; i < 3; i++) {
						var attackList = u.attackFind(this);
						var runList = u.runFind(this);
						sleep(200);
						resetClickType();
						if(attackList.size()>0){
							u.attack(this,units[attackList.get(0).getTilex()][attackList.get(0).getTiley()]);
							sleep(200);
							attacked = true;
						}
						else if(runList.size()>1 && !attacked){
							//todo 此处应该向最进的可攻击对象靠近
							u.run(this,BasicObjectBuilders.loadTile(
									runList.get(0).getTilex(),
									runList.get(0).getTiley()
							));
						}else{
							sleep(200);
							resetClickType();
						}
					}
				}
			}
		}
		aiSay("AI回合结束");
		// AI回合结束，轮转到玩家
		endTurnClicked();
	}


	/**
	 * 在该方法结束当前回合并处理相关操作
	 */
	public void endTurnClicked() {
		if(end){
			System.out.println("游戏已结束");
			aiSay("游戏已结束");
			return;
		}
		var cPlayer = getCurrentPlayer();
		// 回合结束前法力归零
		cPlayer.setMana(0);
		//  如果玩家场上已无卡，意味着游戏结束，游戏失败
		int num = 0;
		int num2 = 0;
//		for (int x = 0; x < 9; x++) {
//			for (int y = 0; y < 5; y++) {
//				if(units[x][y]!=null){
//					if(units[x][y].getPlayId()==0){
//						num++;
//						break;
//					}
//				}
//				if(num>0) break;
//			}
//		}
//		for (int x = 0; x < 9; x++) {
//			for (int y = 0; y < 5; y++) {
//				if(units[x][y]!=null){
//					if(units[x][y].getPlayId()==1){
//						num2++;
//						break;
//					}
//				}
//				if(num2>0) break;
//			}
//		}
//		if(num==0){
//			// 调用游戏结束方法
//			gameEnd(0);
//			return;
//		}else if(num2==0){
//			gameEnd(1);
//		}
		if(getCurrentPlayer().getDeckQueue().size()<=0) gameEnd((currentPlayerId+1)%2);
		BasicCommands.setPlayerMana(out,currentPlayerId,players[currentPlayerId]);
		turnController();
	}

	/**
	 *  游戏结束逻辑
	 */
	public void gameEnd(int loser) {
		aiSay("游戏结束");
		end = true;
		if(loser==1)
			BasicCommands.addPlayer1Notification(out," I'm winner",2000);
		else
			BasicCommands.addPlayer1Notification(out," I'm loser",2000);
	}
}
