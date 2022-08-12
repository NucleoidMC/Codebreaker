package io.github.haykam821.codebreaker.game.phase;

import eu.pb4.holograms.api.Holograms;
import eu.pb4.holograms.api.holograms.AbstractHologram;
import eu.pb4.holograms.api.holograms.AbstractHologram.VerticalAlign;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import io.github.haykam821.codebreaker.game.map.CodebreakerMap;
import io.github.haykam821.codebreaker.game.map.CodebreakerMapBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class CodebreakerWaitingPhase {
	private static final Formatting GUIDE_FORMATTING = Formatting.GOLD;
	private static final Text[] GUIDE_LINES = {
		Text.translatable("gameType.codebreaker.codebreaker").formatted(GUIDE_FORMATTING).formatted(Formatting.BOLD),
		Text.translatable("text.codebreaker.guide.guess_the_code").formatted(GUIDE_FORMATTING),
		Text.translatable("text.codebreaker.guide.guessing_gives_results").formatted(GUIDE_FORMATTING),
		Text.translatable("text.codebreaker.guide.acacia_button_indicates_hit").formatted(GUIDE_FORMATTING),
		Text.translatable("text.codebreaker.guide.stone_button_indicates_blow").formatted(GUIDE_FORMATTING),
		Text.translatable("text.codebreaker.guide.birch_button_indicates_miss").formatted(GUIDE_FORMATTING),
	};

	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final CodebreakerMap map;
	private final CodebreakerConfig config;
	private final Code correctCode;
	private AbstractHologram guideText;

	public CodebreakerWaitingPhase(GameSpace gameSpace, ServerWorld world, CodebreakerMap map, CodebreakerConfig config, Code correctCode) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
		this.correctCode = correctCode;
	}

	public static GameOpenProcedure open(GameOpenContext<CodebreakerConfig> context) {
		Random random = Random.createLocal();
		CodebreakerConfig config = context.config();

		Code correctCode = config.getCodeProvider().generate(random, config);

		CodebreakerMapBuilder mapBuilder = new CodebreakerMapBuilder(config);
		CodebreakerMap map = mapBuilder.create(random, correctCode, config.getCodePegs());

		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithWorld(worldConfig, (activity, world) -> {
			CodebreakerWaitingPhase waiting = new CodebreakerWaitingPhase(activity.getGameSpace(), world, map, config, correctCode);

			GameWaitingLobby.addTo(activity, config.getPlayerConfig());
			CodebreakerActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.TICK, waiting::tick);
			activity.listen(GameActivityEvents.ENABLE, waiting::open);
			activity.listen(GamePlayerEvents.ADD, waiting::addPlayer);
			activity.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
			activity.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
			activity.listen(GamePlayerEvents.OFFER, waiting::offerPlayer);
		});
	}

	public PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.map.getSpawnPos()).and(() -> {
			offer.player().changeGameMode(GameMode.ADVENTURE);
		});
	}

	public GameResult requestStart() {
		CodebreakerActivePhase.open(this.gameSpace, this.world, this.map, this.config, this.guideText, this.correctCode);
		return GameResult.ok();
	}

	public void addPlayer(ServerPlayerEntity player) {
		CodebreakerActivePhase.spawn(this.world, this.map, player);
	}

	public ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		CodebreakerActivePhase.spawn(this.world, this.map, player);
		return ActionResult.SUCCESS;
	}

	public void tick() {
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			if (this.map.isBelowPlatform(player)) {
				CodebreakerActivePhase.spawn(this.world, this.map, player);
			}
		}
	}

	private void open() {
		// Spawn guide text
		Vec3d center = new Vec3d(this.map.getBounds().center().getX(), this.map.getBounds().min().getY() + 2.8, this.map.getBounds().max().getZ());

		this.guideText = Holograms.create(this.world, center, GUIDE_LINES);
		this.guideText.setAlignment(VerticalAlign.CENTER);

		this.guideText.show();
	}
}