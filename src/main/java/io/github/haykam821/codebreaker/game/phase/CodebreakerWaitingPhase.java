package io.github.haykam821.codebreaker.game.phase;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import io.github.haykam821.codebreaker.game.map.CodebreakerMap;
import io.github.haykam821.codebreaker.game.map.CodebreakerMapBuilder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class CodebreakerWaitingPhase {
	private static final ChatFormatting GUIDE_FORMATTING = ChatFormatting.GOLD;
	private static final Component GUIDE_TEXT = Component.empty()
		.append(Component.translatable("gameType.codebreaker.codebreaker").withStyle(ChatFormatting.BOLD))
		.append(CommonComponents.NEW_LINE)
		.append(Component.translatable("text.codebreaker.guide"))
		.withStyle(GUIDE_FORMATTING);

	private final GameSpace gameSpace;
	private final ServerLevel level;
	private final CodebreakerMap map;
	private final CodebreakerConfig config;

	private final Code correctCode;
	private final boolean duplicatePegs;

	private HolderAttachment guideText;

	public CodebreakerWaitingPhase(GameSpace gameSpace, ServerLevel level, CodebreakerMap map, CodebreakerConfig config, Code correctCode, boolean duplicatePegs) {
		this.gameSpace = gameSpace;
		this.level = level;
		this.map = map;
		this.config = config;

		this.correctCode = correctCode;
		this.duplicatePegs = duplicatePegs;
	}

	public static GameOpenProcedure open(GameOpenContext<CodebreakerConfig> context) {
		RandomSource random = RandomSource.createThreadLocalInstance();
		CodebreakerConfig config = context.config();

		Code correctCode = config.getCodeProvider().generate(random, config);
		boolean duplicatePegs = config.getCodeProvider().hasDuplicatePegs(config);

		CodebreakerMapBuilder mapBuilder = new CodebreakerMapBuilder(config);
		CodebreakerMap map = mapBuilder.create(random, correctCode, context.server().registryAccess(), config.getCodePegs());

		RuntimeLevelConfig levelConfig = new RuntimeLevelConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithLevel(levelConfig, (activity, level) -> {
			CodebreakerWaitingPhase waiting = new CodebreakerWaitingPhase(activity.getGameSpace(), level, map, config, correctCode, duplicatePegs);

			GameWaitingLobby.addTo(activity, config.getPlayerConfig());
			CodebreakerActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.TICK, waiting::tick);
			activity.listen(GameActivityEvents.ENABLE, waiting::open);
			activity.listen(GamePlayerEvents.ADD, waiting::addPlayer);
			activity.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
			activity.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
			activity.listen(GamePlayerEvents.ACCEPT, waiting::onAcceptPlayers);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
		});
	}

	public JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return acceptor.teleport(this.level, this.map.getSpawnPos()).thenRunForEach(player -> {
			player.setGameMode(GameType.ADVENTURE);
		});
	}

	public GameResult requestStart() {
		CodebreakerActivePhase.open(this.gameSpace, this.level, this.map, this.config, this.guideText, this.correctCode, this.duplicatePegs);
		return GameResult.ok();
	}

	public void addPlayer(ServerPlayer player) {
		CodebreakerActivePhase.spawn(this.level, this.map, player);
	}

	public EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
		CodebreakerActivePhase.spawn(this.level, this.map, player);
		return EventResult.ALLOW;
	}

	public void tick() {
		for (ServerPlayer player : this.gameSpace.getPlayers()) {
			if (this.map.isBelowPlatform(player)) {
				CodebreakerActivePhase.spawn(this.level, this.map, player);
			}
		}
	}

	private void open() {
		TextDisplayElement element = new TextDisplayElement(GUIDE_TEXT);

		element.setBillboardMode(BillboardConstraints.CENTER);
		element.setLineWidth(450);

		ElementHolder holder = new ElementHolder();
		holder.addElement(element);

		// Spawn guide text
		Vec3 center = new Vec3(this.map.getBounds().center().x(), this.map.getBounds().min().getY() + 2, this.map.getBounds().max().getZ());
		this.guideText = ChunkAttachment.of(holder, level, center);
	}
}