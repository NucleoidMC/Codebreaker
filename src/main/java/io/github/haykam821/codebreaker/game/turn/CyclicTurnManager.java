package io.github.haykam821.codebreaker.game.turn;

import java.util.List;

import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import net.minecraft.server.level.ServerPlayer;

public class CyclicTurnManager extends TurnManager {
	private ServerPlayer turn;

	public CyclicTurnManager(CodebreakerActivePhase phase, ServerPlayer initialTurn) {
		super(phase);
		this.turn = initialTurn;
	}
	
	@Override
	public ServerPlayer getTurn() {
		return this.turn;
	}

	@Override
	public boolean switchTurn() {
		List<ServerPlayer> players = this.phase.getPlayers();
		int turnIndex = players.indexOf(this.turn);

		ServerPlayer previousTurn = this.turn;
		this.turn = players.get((turnIndex + 1) % players.size());
		return this.turn != previousTurn;
	}
}
