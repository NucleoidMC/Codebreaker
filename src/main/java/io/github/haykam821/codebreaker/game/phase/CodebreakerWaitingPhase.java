package io.github.haykam821.codebreaker.game.phase;

import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.map.CodebreakerMap;
import io.github.haykam821.codebreaker.game.map.CodebreakerMapBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.entity.FloatingText;
import xyz.nucleoid.plasmid.entity.FloatingText.VerticalAlign;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;

public class CodebreakerWaitingPhase {
	private static final Formatting GUIDE_FORMATTING = Formatting.GOLD;
	private static final Text[] GUIDE_LINES = {
		new TranslatableText("gameType.codebreaker.codebreaker").formatted(GUIDE_FORMATTING).formatted(Formatting.BOLD),
		new TranslatableText("text.codebreaker.guide.guess_the_code").formatted(GUIDE_FORMATTING),
		new TranslatableText("text.codebreaker.guide.guessing_gives_results").formatted(GUIDE_FORMATTING),
		new TranslatableText("text.codebreaker.guide.acacia_button_indicates_hit").formatted(GUIDE_FORMATTING),
		new TranslatableText("text.codebreaker.guide.stone_button_indicates_blow").formatted(GUIDE_FORMATTING),
		new TranslatableText("text.codebreaker.guide.birch_button_indicates_miss").formatted(GUIDE_FORMATTING),
	};

	private final GameSpace gameSpace;
	private final CodebreakerMap map;
	private final CodebreakerConfig config;
	private FloatingText guideText;

	public CodebreakerWaitingPhase(GameSpace gameSpace, CodebreakerMap map, CodebreakerConfig config) {
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;
	}

	public static GameOpenProcedure open(GameOpenContext<CodebreakerConfig> context) {
		CodebreakerMapBuilder mapBuilder = new CodebreakerMapBuilder(context.getConfig());
		CodebreakerMap map = mapBuilder.create();

		BubbleWorldConfig worldConfig = new BubbleWorldConfig()
			.setGenerator(map.createGenerator(context.getServer()))
			.setDefaultGameMode(GameMode.ADVENTURE);

		return context.createOpenProcedure(worldConfig, game -> {
			CodebreakerWaitingPhase waiting = new CodebreakerWaitingPhase(game.getSpace(), map, context.getConfig());

			GameWaitingLobby.applyTo(game, context.getConfig().getPlayerConfig());
			CodebreakerActivePhase.setRules(game);

			// Listeners
			game.on(GameOpenListener.EVENT, waiting::open);
			game.on(PlayerAddListener.EVENT, waiting::addPlayer);
			game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
			game.on(RequestStartListener.EVENT, waiting::requestStart);
			game.on(RequestStartListener.EVENT, waiting::requestStart);
		});
	}

	private boolean isFull() {
		return this.gameSpace.getPlayerCount() >= this.config.getPlayerConfig().getMaxPlayers();
	}

	public JoinResult offerPlayer(ServerPlayerEntity player) {
		return this.isFull() ? JoinResult.gameFull() : JoinResult.ok();
	}

	public StartResult requestStart() {
		PlayerConfig playerConfig = this.config.getPlayerConfig();
		if (this.gameSpace.getPlayerCount() < playerConfig.getMinPlayers()) {
			return StartResult.NOT_ENOUGH_PLAYERS;
		}

		CodebreakerActivePhase.open(this.gameSpace, this.map, this.config, this.guideText);
		return StartResult.OK;
	}

	public void addPlayer(ServerPlayerEntity player) {
		CodebreakerActivePhase.spawn(this.gameSpace.getWorld(), this.map, player);
	}

	public ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		CodebreakerActivePhase.spawn(this.gameSpace.getWorld(), this.map, player);
		return ActionResult.SUCCESS;
	}

	private void open() {
		// Spawn guide text
		Vec3d center = new Vec3d(this.map.getBounds().getCenter().getX(), this.map.getBounds().getMin().getY() + 2.8, this.map.getBounds().getMax().getZ());
		this.gameSpace.getWorld().getChunk(new BlockPos(center));

		this.guideText = FloatingText.spawn(this.gameSpace.getWorld(), center, GUIDE_LINES, VerticalAlign.CENTER);
	}
}