package io.github.haykam821.codebreaker;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import io.github.haykam821.codebreaker.block.CodeControlBlock;
import io.github.haykam821.codebreaker.block.CodeControlBlockEntity;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.provider.CodeProvider;
import io.github.haykam821.codebreaker.game.code.provider.RandomCodeProvider;
import io.github.haykam821.codebreaker.game.phase.CodebreakerWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;

public class Main implements ModInitializer {
	private static final String MOD_ID = "codebreaker";

	private static final Identifier CODEBREAKER_ID = new Identifier(MOD_ID, "codebreaker");
	public static final GameType<CodebreakerConfig> CODEBREAKER_TYPE = GameType.register(CODEBREAKER_ID, CodebreakerConfig.CODEC, CodebreakerWaitingPhase::open);

	private static final Identifier RANDOM_ID = new Identifier(MOD_ID, "random");

	private static final Identifier CODE_CONTROL_ID = new Identifier(MOD_ID, "code_control");
	public static final Block CODE_CONTROL = new CodeControlBlock(FabricBlockSettings.copy(Blocks.LECTERN));
	public static final BlockEntityType<CodeControlBlockEntity> CODE_CONTROL_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(CodeControlBlockEntity::new, CODE_CONTROL).build();
	public static final Item CODE_CONTROL_ITEM = new PolymerBlockItem(CODE_CONTROL, new Item.Settings(), Items.LECTERN);

	@Override
	public void onInitialize() {
		CodeProvider.REGISTRY.register(RANDOM_ID, RandomCodeProvider.CODEC);

		Registry.register(Registries.BLOCK, CODE_CONTROL_ID, CODE_CONTROL);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, CODE_CONTROL_ID, CODE_CONTROL_BLOCK_ENTITY);
		Registry.register(Registries.ITEM, CODE_CONTROL_ID, CODE_CONTROL_ITEM);
	}
}