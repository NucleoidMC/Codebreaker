package io.github.haykam821.codebreaker.game.turn;

import java.util.List;

import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

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
	public void switchTurn() {
		List<ServerPlayerEntity> players = this.phase.getPlayers();
		int turnIndex = players.indexOf(this.turn);

		ServerPlayerEntity previousTurn = this.turn;
		this.turn = players.get((turnIndex + 1) % players.size());

		if (previousTurn != this.turn) {
			Text message = new TranslatableText("text.codebreaker.next_turn", this.turn.getDisplayName()).formatted(Formatting.GOLD);
			this.phase.getGameSpace().getPlayers().sendMessage(message);
		}
	}
}
