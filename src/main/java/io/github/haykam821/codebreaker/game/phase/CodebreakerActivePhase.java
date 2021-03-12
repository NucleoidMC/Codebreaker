package io.github.haykam821.codebreaker.game.phase;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.haykam821.codebreaker.Main;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import io.github.haykam821.codebreaker.game.code.ComparedCode;
import io.github.haykam821.codebreaker.game.map.CodebreakerMap;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.entity.FloatingText;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.PlayerRemoveListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

public class CodebreakerActivePhase {
	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final CodebreakerMap map;
	private final CodebreakerConfig config;
	private final FloatingText guideText;
	private final List<ServerPlayerEntity> players;
	private final Code correctCode;
	private Code queuedCode;
	private int queuedIndex = 0;
	private TurnManager turnManager;
	private int ticks = 0;

	public CodebreakerActivePhase(GameSpace gameSpace, CodebreakerMap map, CodebreakerConfig config, FloatingText guideText, List<ServerPlayerEntity> players) {
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

	public static void open(GameSpace gameSpace, CodebreakerMap map, CodebreakerConfig config, FloatingText guide) {
		CodebreakerActivePhase phase = new CodebreakerActivePhase(gameSpace, map, config, guide, Lists.newArrayList(gameSpace.getPlayers()));

		gameSpace.openGame(game -> {
			CodebreakerActivePhase.setRules(game);

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
			CodebreakerActivePhase.spawn(this.world, this.map, player);

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
				CodebreakerActivePhase.spawn(this.world, this.map, player);
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
		CodebreakerActivePhase.spawn(this.world, this.map, player);
	}

	private void submitCode(ServerPlayerEntity player) {
		ComparedCode comparedCode = new ComparedCode(this.queuedCode.getPegs(), this.correctCode);
		comparedCode.build(this.world, this.map.getCodeOrigin().add(this.queuedIndex, 0, 0));

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

	private void eraseQueuedCode(ServerPlayerEntity player) {
		this.createQueuedCode();
		player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 1, 0.5f);
	}

	private void queueCodePeg(ServerPlayerEntity player, BlockState state) {
		if (this.queuedCode == null) {
			this.createQueuedCode();
		}
		this.queuedCode.setNext(state);
		player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 1, 2);
	}

	private ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		if (hand != Hand.MAIN_HAND) return ActionResult.FAIL;
		if (!this.players.contains(player)) return ActionResult.FAIL;

		BlockState state = player.getEntityWorld().getBlockState(hitResult.getBlockPos());
		if (!state.isIn(Main.CODE_PEGS) && !state.isOf(Blocks.BEDROCK)) return ActionResult.FAIL;

		if (!this.turnManager.isTurn(player)) {
			player.sendMessage(this.turnManager.getOtherTurnMessage(), false);
			return ActionResult.FAIL;
		}
	
		if (state.isOf(Blocks.BEDROCK)) {
			this.eraseQueuedCode(player);
		} else {
			this.queueCodePeg(player, state);
		}

		if (this.queuedCode.isCompletelyFilled()) {
			this.submitCode(player);
		} else {
			this.queuedCode.build(this.world, this.map.getCodeOrigin().add(this.queuedIndex, 0, 0));
		}
		
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		if (this.players.contains(player)) {
			CodebreakerActivePhase.spawn(this.world, this.map, player);
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

	public CodebreakerConfig getConfig() {
		return this.config;
	}

	public List<ServerPlayerEntity> getPlayers() {
		return this.players;
	}

	public static void spawn(ServerWorld world, CodebreakerMap map, ServerPlayerEntity player) {
		Vec3d center = map.getBounds().getCenter();
		player.teleport(world, center.getX(), 65, center.getZ(), 180, 0);
	}
}