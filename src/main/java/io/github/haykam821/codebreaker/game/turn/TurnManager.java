package io.github.haykam821.codebreaker.game.turn;

import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public abstract class TurnManager {
	protected final CodebreakerActivePhase phase;

	public TurnManager(CodebreakerActivePhase phase) {
		this.phase = phase;
	}

	public abstract ServerPlayer getTurn();

	public boolean isTurn(ServerPlayer player) {
		return player == this.getTurn();
	}

	public final void switchTurnAndPlayEffects() {
		if (this.switchTurn()) {
			this.playNextTurnEffects();
		}
	}

	public final void playNextTurnEffects() {
		Component nextTurnMessage = this.getNextTurnMessage();
		if (nextTurnMessage != null) {
			this.phase.getGameSpace().getPlayers().sendMessage(nextTurnMessage);
		}

		ServerPlayer turn = this.getTurn();
		if (turn != null) {
			TurnSounds.playTurnSounds(turn);
		}
	}

	/**
	 * @return whether the new turn is different from the old turn
	 */
	public abstract boolean switchTurn();

	public final Component getNextTurnMessage() {
		ServerPlayer turn = this.getTurn();
		if (turn == null) return null;

		return Component.translatable("text.codebreaker.next_turn", turn.getDisplayName()).withStyle(ChatFormatting.GOLD);
	}

	public final Component getOtherTurnMessage() {
		ServerPlayer turn = this.getTurn();
		if (turn == null) {
			return Component.translatable("text.codebreaker.no_turn").withStyle(ChatFormatting.RED);
		}

		return Component.translatable("text.codebreaker.other_turn", turn.getDisplayName()).withStyle(ChatFormatting.RED);
	}
}
