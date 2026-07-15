package io.github.haykam821.codebreaker.game.phase;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import io.github.haykam821.codebreaker.Main;
import io.github.haykam821.codebreaker.block.CodeControlBlockEntity;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import io.github.haykam821.codebreaker.game.code.ComparedCode;
import io.github.haykam821.codebreaker.game.map.CodebreakerMap;
import io.github.haykam821.codebreaker.game.turn.TurnManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.util.PlayerUtil;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class CodebreakerActivePhase {
	private final GameSpace gameSpace;
	private final ServerLevel level;
	private final CodebreakerMap map;
	private final CodebreakerConfig config;
	private final HolderAttachment guideText;
	private final List<ServerPlayer> players;

	private final Code correctCode;
	private final boolean duplicatePegs;

	private Code queuedCode;
	private int queuedIndex = 0;
	private TurnManager turnManager;
	private int ticks = 0;
	private int ticksUntilClose = -1;

	public CodebreakerActivePhase(GameSpace gameSpace, ServerLevel level, CodebreakerMap map, CodebreakerConfig config, HolderAttachment guideText, List<ServerPlayer> players, Code correctCode, boolean duplicatePegs) {
		this.gameSpace = gameSpace;
		this.level = level;
		this.map = map;
		this.config = config;
		this.guideText = guideText;
		this.players = players;

		this.correctCode = correctCode;
		this.duplicatePegs = duplicatePegs;
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.BLOCK_DROPS);
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.PORTALS);
		activity.deny(GameRuleType.THROW_ITEMS);
	}

	public static void open(GameSpace gameSpace, ServerLevel level, CodebreakerMap map, CodebreakerConfig config, HolderAttachment guide, Code correctCode, boolean duplicatePegs) {
		CodebreakerActivePhase phase = new CodebreakerActivePhase(gameSpace, level, map, config, guide, Lists.newArrayList(gameSpace.getPlayers().participants()), correctCode, duplicatePegs);

		gameSpace.setActivity(activity -> {
			CodebreakerActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, phase::enable);
			activity.listen(GameActivityEvents.TICK, phase::tick);
			activity.listen(GamePlayerEvents.ACCEPT, phase::onAcceptPlayers);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::acceptSpectators);
			activity.listen(PlayerDamageEvent.EVENT, phase::onPlayerDamage);
			activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
			activity.listen(GamePlayerEvents.REMOVE, phase::onPlayerRemove);
			activity.listen(BlockUseEvent.EVENT, phase::onUseBlock);
		});
	}

	private void enable() {
		for (ServerPlayer player : this.players) {
			player.setGameMode(GameType.ADVENTURE);
			CodebreakerActivePhase.spawn(this.level, this.map, player);

			if (this.turnManager == null) {
				this.turnManager = config.createTurnManager(this, player);
				this.turnManager.playNextTurnEffects();
			}
		}

		for (ServerPlayer player : this.gameSpace.getPlayers().spectators()) {
			CodebreakerActivePhase.spawn(this.level, this.map, player);
			this.setSpectator(player);
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

		for (ServerPlayer player : this.players) {
			if (this.map.isBelowPlatform(player)) {
				CodebreakerActivePhase.spawn(this.level, this.map, player);
			}
		}
	}

	private void endGame() {
		this.ticksUntilClose = this.config.getTicksUntilClose().sample(this.level.getRandom());
	}

	private void endGameWithWinner(ServerPlayer player) {
		this.gameSpace.getPlayers().sendMessage(Component.translatable("text.codebreaker.win", player.getDisplayName(), this.queuedIndex + 1).withStyle(ChatFormatting.GOLD));
		this.endGame();
	}

	private boolean isGameEnding() {
		return this.ticksUntilClose >= 0;
	}

	public void setSpectator(ServerPlayer player) {
		player.setGameMode(GameType.SPECTATOR);
	}

	private JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return acceptor.teleport(this.level, this.map.getSpawnPos()).thenRunForEach(player -> {
			player.setYRot(180);
			this.setSpectator(player);
		});
	}

	private void submitCode(ServerPlayer player) {
		ComparedCode comparedCode = new ComparedCode(this.queuedCode.getPegs(), this.correctCode);
		comparedCode.build(this.level, this.map.getCodeOrigin().offset(this.queuedIndex, 0, 0), this.config.getMapConfig());

		if (comparedCode.isCorrect()) {
			this.endGameWithWinner(player);
			this.gameSpace.getPlayers().playSound(SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.BLOCKS, 1, 1);
		} else if (this.queuedIndex + 1 >= this.config.getChances()) {
			this.gameSpace.getPlayers().sendMessage(Component.translatable("text.codebreaker.lose", this.queuedIndex + 1).withStyle(ChatFormatting.RED));
			this.gameSpace.getPlayers().playSound(SoundEvents.CREEPER_DEATH, SoundSource.BLOCKS, 1, 1);

			this.endGame();
		} else {
			this.turnManager.switchTurnAndPlayEffects();
			this.gameSpace.getPlayers().playSound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1, 1);
		}

		this.queuedCode = null;
		this.queuedIndex += 1;
	}

	private void createQueuedCode() {
		this.queuedCode = new Code(this.correctCode.getLength());
	}

	private void eraseQueuedCode(ServerPlayer player) {
		this.createQueuedCode();
		PlayerUtil.playSoundToPlayer(player, SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.BLOCKS, 1, 0.5f);
	}

	private boolean tryQueueCodePeg(ServerPlayer player, BlockState state) {
		if (this.queuedCode == null) {
			this.createQueuedCode();
		}

		if (this.queuedCode.setNext(state, this.duplicatePegs)) {
			PlayerUtil.playSoundToPlayer(player, SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.BLOCKS, 1, 2);
			return true;
		}

		player.sendSystemMessage(Component.translatable("text.codebreaker.no_duplicate_pegs").withStyle(ChatFormatting.RED), false);
		return false;
	}

	private InteractionResult onUseBlock(ServerPlayer player, InteractionHand hand, BlockHitResult hitResult) {
		if (this.isGameEnding()) return InteractionResult.FAIL;
		if (hand != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;
		if (!this.players.contains(player)) return InteractionResult.FAIL;

		Optional<CodeControlBlockEntity> maybeBlockEntity = level.getBlockEntity(hitResult.getBlockPos(), Main.CODE_CONTROL_BLOCK_ENTITY);

		if (maybeBlockEntity.isPresent()) {
			BlockState state = maybeBlockEntity.get().getBlock();

			if (this.useCodeControl(player, state)) {
				// Swing hand and notify player
				player.swing(hand, true);
			}
		}

		return InteractionResult.FAIL;
	}

	private boolean useCodeControl(ServerPlayer player, BlockState state) {
		boolean erase = state.is(Blocks.BEDROCK);
		boolean validCodeControl = state.is(this.config.getCodePegs()) || erase;

		if (!validCodeControl) {
			return false;
		}

		if (!this.turnManager.isTurn(player)) {
			player.sendSystemMessage(this.turnManager.getOtherTurnMessage(), false);
			return false;
		}
	
		if (erase) {
			this.eraseQueuedCode(player);
		} else if (!this.tryQueueCodePeg(player, state)) {
			return false;
		}

		if (this.queuedCode.isCompletelyFilled()) {
			this.submitCode(player);
		} else {
			this.queuedCode.build(this.level, this.map.getCodeOrigin().offset(this.queuedIndex, 0, 0), this.config.getMapConfig());
		}

		return true;
	}

	private EventResult onPlayerDamage(ServerPlayer player, DamageSource source, float amount) {
		return EventResult.DENY;
	}

	private EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
		if (this.players.contains(player)) {
			CodebreakerActivePhase.spawn(this.level, this.map, player);
		}
		return EventResult.DENY;
	}

	private void onPlayerRemove(ServerPlayer player) {
		if (!this.isGameEnding() && this.players.remove(player) && !this.players.isEmpty()) {
			this.turnManager.switchTurnAndPlayEffects();
		}
	}

	public GameSpace getGameSpace() {
		return this.gameSpace;
	}

	public CodebreakerConfig getConfig() {
		return this.config;
	}

	public List<ServerPlayer> getPlayers() {
		return this.players;
	}

	public static void spawn(ServerLevel world, CodebreakerMap map, ServerPlayer player) {
		Vec3 spawnPos = map.getSpawnPos();
		player.teleportTo(world, spawnPos.x(), spawnPos.y(), spawnPos.z(), Set.of(), 180, 0, true);
	}
}