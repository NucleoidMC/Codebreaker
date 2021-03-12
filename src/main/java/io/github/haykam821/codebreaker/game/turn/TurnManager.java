package io.github.haykam821.codebreaker.game.turn;

import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public abstract class TurnManager {
	protected final CodebreakerActivePhase phase;

	public TurnManager(CodebreakerActivePhase phase) {
		this.phase = phase;
	}

	public abstract ServerPlayerEntity getTurn();

	public boolean isTurn(ServerPlayerEntity player) {
		return player == this.getTurn();
	}

	public abstract void switchTurn();

	public final Text getOtherTurnMessage() {
		ServerPlayerEntity turn = this.getTurn();
		if (turn == null) {
			return new TranslatableText("text.codebreaker.no_turn").formatted(Formatting.RED);
		}

		return new TranslatableText("text.codebreaker.other_turn", turn.getDisplayName()).formatted(Formatting.RED);
	}
}
