package io.github.haykam821.codebreaker.game.phase;

import com.google.common.collect.Lists;
import io.github.haykam821.codebreaker.Codebreaker;
import io.github.haykam821.codebreaker.game.CbConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import io.github.haykam821.codebreaker.game.code.ComparedCode;
import io.github.haykam821.codebreaker.game.map.CbMap;
import io.github.haykam821.codebreaker.game.turn.TurnManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.entity.FloatingText;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

import java.util.List;

public class CbActivePhase {
	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final CbMap map;
	private final CbConfig config;
	private final FloatingText guideText;
	private final List<ServerPlayerEntity> players;
	private final Code correctCode;
	private Code queuedCode;
	private int queuedIndex = 0;
	private TurnManager turnManager;
	private int ticks = 0;

	public CbActivePhase(GameSpace gameSpace, CbMap map, CbConfig config, FloatingText guideText, List<ServerPlayerEntity> players) {
		this.gameSpace = gameSpace;
		this.world = gameSpace.getWorld();
		this.map = map;
		this.config = config;
		this.guideText = guideText;
		this.players = players;
		this.correctCode = Code.createRandom(config.getSpaces(), gameSpace.getWorld().getRandom());
	}

	public static void setRules(GameLogic game) {
		game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
		game.setRule(GameRule.CRAFTING, RuleResult.DENY);
		game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
		game.setRule(GameRule.HUNGER, RuleResult.DENY);
		game.setRule(GameRule.PORTALS, RuleResult.DENY);
		game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
	}

	public static void open(GameSpace gameSpace, CbMap map, CbConfig config, FloatingText guide) {
		CbActivePhase phase = new CbActivePhase(gameSpace, map, config, guide, Lists.newArrayList(gameSpace.getPlayers()));

		gameSpace.openGame(game -> {
			CbActivePhase.setRules(game);

			// Listeners
			game.on(GameOpenListener.EVENT, phase::open);
			game.on(GameTickListener.EVENT, phase::tick);
			game.on(PlayerAddListener.EVENT, phase::addPlayer);
			game.on(PlayerDamageListener.EVENT, phase::onPlayerDamage);
			game.on(PlayerDeathListener.EVENT, phase::onPlayerDeath);
			game.on(PlayerRemoveListener.EVENT, phase::onPlayerRemove);
			game.on(UseBlockListener.EVENT, phase::onUseBlock);
		});
	}

	private void open() {
		for (ServerPlayerEntity player : this.players) {
			player.setGameMode(GameMode.ADVENTURE);
			CbActivePhase.spawn(this.world, this.map, player);

			if (this.turnManager == null) {
				this.turnManager = config.createTurnManager(this, player);
				this.turnManager.announceNextTurn();
			}
		}
	}

	private void tick() {
		this.ticks += 1;
		if (this.guideText != null && ticks == this.config.getGuideTicks()) {
			this.guideText.remove();
		}

		for (ServerPlayerEntity player : this.players) {
			if (this.map.isBelowPlatform(player)) {
				CbActivePhase.spawn(this.world, this.map, player);
			}
		}
	}

	private void endGame() {
		this.gameSpace.close(GameCloseReason.FINISHED);
	}

	private void endGameWithWinner(ServerPlayerEntity player) {
		this.gameSpace.getPlayers().sendMessage(new TranslatableText("text.codebreaker.win", player.getDisplayName(), this.queuedIndex + 1).formatted(Formatting.GOLD));
		this.endGame();
	}

	public void setSpectator(ServerPlayerEntity player) {
		player.setGameMode(GameMode.SPECTATOR);
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.setSpectator(player);
		CbActivePhase.spawn(this.world, this.map, player);
	}

	private void submitCode(ServerPlayerEntity player) {
		ComparedCode comparedCode = new ComparedCode(this.queuedCode.getPegs(), this.correctCode);
		comparedCode.build(this.config, this.world, this.map.getCodeOrigin().add(this.queuedIndex, 0, 0));

		if (comparedCode.isCorrect()) {
			this.endGameWithWinner(player);
			this.gameSpace.getPlayers().sendSound(SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.BLOCKS, 1, 1);
		} else if (this.queuedIndex + 1 >= this.config.getChances()) {
			this.gameSpace.getPlayers().sendMessage(new TranslatableText("text.codebreaker.lose", this.queuedIndex + 1).formatted(Formatting.RED));
			this.gameSpace.getPlayers().sendSound(SoundEvents.ENTITY_CREEPER_DEATH, SoundCategory.BLOCKS, 1, 1);

			this.endGame();
		} else {
			this.turnManager.switchTurnAndAnnounce();
			this.gameSpace.getPlayers().sendSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1, 1);
		}

		this.queuedCode = null;
		this.queuedIndex += 1;
	}

	private void createQueuedCode() {
		this.queuedCode = new Code(this.config.getSpaces());
	}

	private void eraseQueuedCode(World world, BlockPos pos) {
		this.createQueuedCode();
		this.queuedCode.build(this.config, this.world, this.map.getCodeOrigin().add(this.queuedIndex, 0, 0));
		world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 1, 0.5f);
	}

	private void queueCodePeg(World world, BlockPos pos, BlockState state) {
		if (this.queuedCode == null) {
			this.createQueuedCode();
		}
		this.queuedCode.setNext(state);
		this.queuedCode.build(this.config, this.world, this.map.getCodeOrigin().add(this.queuedIndex, 0, 0));
		world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 1, 2);
	}

	private ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		if (hand != Hand.MAIN_HAND || !this.players.contains(player)) return ActionResult.FAIL;

		BlockState state = player.getEntityWorld().getBlockState(hitResult.getBlockPos());
		World world = player.getEntityWorld();

		if (!this.turnManager.isTurn(player)) {
			player.sendMessage(this.turnManager.getOtherTurnMessage(), false);
			return ActionResult.FAIL;
		}
		else {
			if(this.map.getPegBounds().contains(hitResult.getBlockPos())) {
				if (state.isOf(Blocks.BEDROCK)) {
					this.eraseQueuedCode(world, hitResult.getBlockPos());
				}
				else if (state.isOf(Blocks.SEA_LANTERN)) {
					this.submitCode(player);
					this.createQueuedCode();
				}
				else if (state.isIn(Codebreaker.CODE_PEGS)) {
					this.queueCodePeg(world, hitResult.getBlockPos(), state);
				}
				this.queuedCode.buildControl(this.config, this.world, this.map.getPegBounds());
				return ActionResult.SUCCESS;
			}
		}
		
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		if (this.players.contains(player)) {
			CbActivePhase.spawn(this.world, this.map, player);
		}
		return ActionResult.FAIL;
	}

	private void onPlayerRemove(ServerPlayerEntity player) {
		if (this.players.remove(player) && !this.players.isEmpty()) {
			this.turnManager.switchTurnAndAnnounce();
		}
	}

	public GameSpace getGameSpace() {
		return this.gameSpace;
	}

	public CbConfig getConfig() {
		return this.config;
	}

	public List<ServerPlayerEntity> getPlayers() {
		return this.players;
	}

	public static void spawn(ServerWorld world, CbMap map, ServerPlayerEntity player) {
		Vec3d center = map.getFloorBounds().getCenter();
		player.teleport(world, center.getX(), 65, center.getZ(), 180, 0);
	}
}