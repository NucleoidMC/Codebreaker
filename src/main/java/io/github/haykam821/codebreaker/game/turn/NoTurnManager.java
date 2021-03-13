package io.github.haykam821.codebreaker.game.turn;

import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import net.minecraft.server.network.ServerPlayerEntity;

public class NoTurnManager extends TurnManager {
	public NoTurnManager(CodebreakerActivePhase phase) {
		super(phase);
	}

	@Override
	public ServerPlayerEntity getTurn() {
		return null;
	}

	@Override
	public boolean isTurn(ServerPlayerEntity player) {
		return true;
	}

	@Override
	public boolean switchTurn() {
		return false;
	}
}
