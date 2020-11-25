package io.github.haykam821.codebreaker.game.phase;

import java.util.List;
import java.util.stream.Collectors;

import io.github.haykam821.codebreaker.Main;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import io.github.haykam821.codebreaker.game.code.ComparedCode;
import io.github.haykam821.codebreaker.game.map.CodebreakerMap;
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
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class CodebreakerActivePhase {
	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final CodebreakerMap map;
	private final CodebreakerConfig config;
	private final List<PlayerRef> players;
	private final Code correctCode;
	private Code queuedCode;
	private int queuedIndex = 0;
	private ServerPlayerEntity currentPlayer;

	public CodebreakerActivePhase(GameSpace gameSpace, CodebreakerMap map, CodebreakerConfig config, List<PlayerRef> players) {
		this.gameSpace = gameSpace;
		this.world = gameSpace.getWorld();
		this.map = map;
		this.config = config;
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

	public static void open(GameSpace gameSpace, CodebreakerMap map, CodebreakerConfig config) {
		List<PlayerRef> players = gameSpace.getPlayers().stream().map(PlayerRef::of).collect(Collectors.toList());
		CodebreakerActivePhase phase = new CodebreakerActivePhase(gameSpace, map, config, players);

		gameSpace.openGame(game -> {
			CodebreakerActivePhase.setRules(game);

			// Listeners
			game.on(GameOpenListener.EVENT, phase::open);
			game.on(PlayerAddListener.EVENT, phase::addPlayer);
			game.on(PlayerDamageListener.EVENT, phase::onPlayerDamage);
			game.on(PlayerDeathListener.EVENT, phase::onPlayerDeath);
			game.on(UseBlockListener.EVENT, phase::onUseBlock);
		});
	}

	private void open() {
		for (PlayerRef playerRef : this.players) {
			playerRef.ifOnline(this.world, player -> {
				player.setGameMode(GameMode.ADVENTURE);
				CodebreakerActivePhase.spawn(this.world, this.map, player);

				if (this.currentPlayer == null) {
					this.currentPlayer = playerRef.getEntity(this.world);
				}
			});
		}
	}

	private void endGame() {
		this.gameSpace.close();
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

	private void switchPlayer() {
		int currentIndex = this.players.indexOf(PlayerRef.of(this.currentPlayer));

		ServerPlayerEntity previousPlayer = this.currentPlayer;
		this.currentPlayer = this.players.get(currentIndex % this.players.size()).getEntity(this.world);

		if (previousPlayer != this.currentPlayer) {
			this.gameSpace.getPlayers().sendMessage(new TranslatableText("text.codebreaker.next_turn", this.currentPlayer).formatted(Formatting.GOLD));
		}
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
			this.switchPlayer();
			this.gameSpace.getPlayers().sendSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1, 1);
		}

		this.queuedCode = null;
		this.queuedIndex += 1;
	}

	private ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		if (hand != Hand.MAIN_HAND) return ActionResult.FAIL;

		BlockState state = player.getEntityWorld().getBlockState(hitResult.getBlockPos());
		if (!state.isIn(Main.CODE_PEGS) && !state.isOf(Blocks.BEDROCK)) return ActionResult.FAIL;

		if (player != this.currentPlayer) {
			player.sendMessage(new TranslatableText("text.codebreaker.other_turn", this.currentPlayer.getDisplayName()).formatted(Formatting.RED), false);
		}
	
		if (state.isOf(Blocks.BEDROCK)) {
			this.queuedCode = new Code(this.config.getSpaces());
			player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 1, 0.5f);
		} else {
			if (this.queuedCode == null) {
				this.queuedCode = new Code(this.config.getSpaces());
			}
			this.queuedCode.setNext(state);
			player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 1, 2);
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
		if (this.players.contains(PlayerRef.of(player))) {
			CodebreakerActivePhase.spawn(this.world, this.map, player);
		}
		return ActionResult.FAIL;
	}

	public GameSpace getGameSpace() {
		return this.gameSpace;
	}

	public CodebreakerConfig getConfig() {
		return this.config;
	}

	public static void spawn(ServerWorld world, CodebreakerMap map, ServerPlayerEntity player) {
		Vec3d center = map.getBounds().getCenter();
		player.teleport(world, center.getX(), 65, center.getZ(), 180, 0);
	}
}