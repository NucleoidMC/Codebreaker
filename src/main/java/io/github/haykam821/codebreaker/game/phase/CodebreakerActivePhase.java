package io.github.haykam821.codebreaker.game.phase;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import io.github.haykam821.codebreaker.Main;
import io.github.haykam821.codebreaker.block.CodeControlBlockEntity;
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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class CodebreakerActivePhase {
	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final CodebreakerMap map;
	private final CodebreakerConfig config;
	private final HolderAttachment guideText;
	private final List<ServerPlayerEntity> players;
	private final Code correctCode;
	private Code queuedCode;
	private int queuedIndex = 0;
	private TurnManager turnManager;
	private int ticks = 0;
	private int ticksUntilClose = -1;

	public CodebreakerActivePhase(GameSpace gameSpace, ServerWorld world, CodebreakerMap map, CodebreakerConfig config, HolderAttachment guideText, List<ServerPlayerEntity> players, Code correctCode) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
		this.guideText = guideText;
		this.players = players;
		this.correctCode = correctCode;
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.BLOCK_DROPS);
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.PORTALS);
		activity.deny(GameRuleType.THROW_ITEMS);
	}

	public static void open(GameSpace gameSpace, ServerWorld world, CodebreakerMap map, CodebreakerConfig config, HolderAttachment guide, Code correctCode) {
		CodebreakerActivePhase phase = new CodebreakerActivePhase(gameSpace, world, map, config, guide, Lists.newArrayList(gameSpace.getPlayers()), correctCode);

		gameSpace.setActivity(activity -> {
			CodebreakerActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, phase::enable);
			activity.listen(GameActivityEvents.TICK, phase::tick);
			activity.listen(GamePlayerEvents.OFFER, phase::offerPlayer);
			activity.listen(PlayerDamageEvent.EVENT, phase::onPlayerDamage);
			activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
			activity.listen(GamePlayerEvents.REMOVE, phase::onPlayerRemove);
			activity.listen(BlockUseEvent.EVENT, phase::onUseBlock);
		});
	}

	private void enable() {
		for (ServerPlayerEntity player : this.players) {
			player.changeGameMode(GameMode.ADVENTURE);
			CodebreakerActivePhase.spawn(this.world, this.map, player);

			if (this.turnManager == null) {
				this.turnManager = config.createTurnManager(this, player);
				this.turnManager.announceNextTurn();
			}
		}
	}

	private void tick() {
		// Decrease ticks until game end to zero
		if (this.isGameEnding()) {
			if (this.ticksUntilClose == 0) {
				this.gameSpace.close(GameCloseReason.FINISHED);
			}

			this.ticksUntilClose -= 1;
		}

		this.ticks += 1;
		if (this.guideText != null && ticks == this.config.getGuideTicks()) {
			this.guideText.destroy();
		}

		for (ServerPlayerEntity player : this.players) {
			if (this.map.isBelowPlatform(player)) {
				CodebreakerActivePhase.spawn(this.world, this.map, player);
			}
		}
	}

	private void endGame() {
		this.ticksUntilClose = this.config.getTicksUntilClose().get(this.world.getRandom());
	}

	private void endGameWithWinner(ServerPlayerEntity player) {
		this.gameSpace.getPlayers().sendMessage(Text.translatable("text.codebreaker.win", player.getDisplayName(), this.queuedIndex + 1).formatted(Formatting.GOLD));
		this.endGame();
	}

	private boolean isGameEnding() {
		return this.ticksUntilClose >= 0;
	}

	public void setSpectator(ServerPlayerEntity player) {
		player.changeGameMode(GameMode.SPECTATOR);
	}

	private PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.map.getSpawnPos()).and(() -> {
			offer.player().setYaw(180);
			this.setSpectator(offer.player());
		});
	}

	private void submitCode(ServerPlayerEntity player) {
		ComparedCode comparedCode = new ComparedCode(this.queuedCode.getPegs(), this.correctCode);
		comparedCode.build(this.world, this.map.getCodeOrigin().add(this.queuedIndex, 0, 0), this.config.getMapConfig());

		if (comparedCode.isCorrect()) {
			this.endGameWithWinner(player);
			this.gameSpace.getPlayers().playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.BLOCKS, 1, 1);
		} else if (this.queuedIndex + 1 >= this.config.getChances()) {
			this.gameSpace.getPlayers().sendMessage(Text.translatable("text.codebreaker.lose", this.queuedIndex + 1).formatted(Formatting.RED));
			this.gameSpace.getPlayers().playSound(SoundEvents.ENTITY_CREEPER_DEATH, SoundCategory.BLOCKS, 1, 1);

			this.endGame();
		} else {
			this.turnManager.switchTurnAndAnnounce();
			this.gameSpace.getPlayers().playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1, 1);
		}

		this.queuedCode = null;
		this.queuedIndex += 1;
	}

	private void createQueuedCode() {
		this.queuedCode = new Code(this.correctCode.getLength());
	}

	private void eraseQueuedCode(ServerPlayerEntity player) {
		this.createQueuedCode();
		player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 1, 0.5f);
	}

	private void queueCodePeg(ServerPlayerEntity player, BlockState state) {
		if (this.queuedCode == null) {
			this.createQueuedCode();
		}
		this.queuedCode.setNext(state);
		player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 1, 2);
	}

	private ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		if (this.isGameEnding()) return ActionResult.FAIL;
		if (hand != Hand.MAIN_HAND) return ActionResult.FAIL;
		if (!this.players.contains(player)) return ActionResult.FAIL;

		Optional<CodeControlBlockEntity> maybeBlockEntity = world.getBlockEntity(hitResult.getBlockPos(), Main.CODE_CONTROL_BLOCK_ENTITY);

		if (maybeBlockEntity.isPresent()) {
			BlockState state = maybeBlockEntity.get().getBlock();
			if (!state.isIn(this.config.getCodePegs()) && !state.isOf(Blocks.BEDROCK)) return ActionResult.FAIL;

			if (this.useCodeControl(player, state)) {
				// Swing hand and notify player
				player.swingHand(hand, true);
			}
		}

		return ActionResult.FAIL;
	}

	private boolean useCodeControl(ServerPlayerEntity player, BlockState state) {
		if (!this.turnManager.isTurn(player)) {
			player.sendMessage(this.turnManager.getOtherTurnMessage(), false);
			return false;
		}
	
		if (state.isOf(Blocks.BEDROCK)) {
			this.eraseQueuedCode(player);
		} else {
			this.queueCodePeg(player, state);
		}

		if (this.queuedCode.isCompletelyFilled()) {
			this.submitCode(player);
		} else {
			this.queuedCode.build(this.world, this.map.getCodeOrigin().add(this.queuedIndex, 0, 0), this.config.getMapConfig());
		}

		return true;
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
		Vec3d spawnPos = map.getSpawnPos();
		player.teleport(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 180, 0);
	}
}