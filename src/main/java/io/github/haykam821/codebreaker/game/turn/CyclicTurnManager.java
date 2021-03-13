package io.github.haykam821.codebreaker.game.turn;

import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class CyclicTurnManager extends TurnManager {
	private ServerPlayerEntity turn;

	public CyclicTurnManager(CodebreakerActivePhase phase, ServerPlayerEntity initialTurn) {
		super(phase);
		this.turn = initialTurn;
	}
	
	@Override
	public ServerPlayerEntity getTurn() {
		return this.turn;
	}

	@Override
	public boolean switchTurn() {
		List<ServerPlayerEntity> players = this.phase.getPlayers();
		int turnIndex = players.indexOf(this.turn);

		ServerPlayerEntity previousTurn = this.turn;
		this.turn = players.get((turnIndex + 1) % players.size());
		return this.turn != previousTurn;
	}
}
