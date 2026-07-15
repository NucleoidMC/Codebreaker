package io.github.haykam821.codebreaker;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import io.github.haykam821.codebreaker.block.CodeControlBlock;
import io.github.haykam821.codebreaker.block.CodeControlBlockEntity;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.provider.CodeProvider;
import io.github.haykam821.codebreaker.game.code.provider.RandomCodeProvider;
import io.github.haykam821.codebreaker.game.phase.CodebreakerWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.GameTypes;

public class Main implements ModInitializer {
	private static final String MOD_ID = "codebreaker";

	private static final Identifier CODEBREAKER_ID = Main.identifier("codebreaker");
	public static final GameType<CodebreakerConfig> CODEBREAKER_TYPE = GameTypes.register(CODEBREAKER_ID, CodebreakerConfig.CODEC, CodebreakerWaitingPhase::open);

	private static final Identifier RANDOM_ID = Main.identifier("random");

	private static final Identifier CODE_CONTROL_ID = Main.identifier("code_control");

	private static final ResourceKey<Block> CODE_CONTROL_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, CODE_CONTROL_ID);
	public static final Block CODE_CONTROL = new CodeControlBlock(Block.Properties.ofFullCopy(Blocks.LECTERN).setId(CODE_CONTROL_BLOCK_KEY));

	public static final BlockEntityType<CodeControlBlockEntity> CODE_CONTROL_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(CodeControlBlockEntity::new, CODE_CONTROL).build();

	private static final ResourceKey<Item> CODE_CONTROL_ITEM_KEY = ResourceKey.create(Registries.ITEM, CODE_CONTROL_ID);
	public static final Item CODE_CONTROL_ITEM = new PolymerBlockItem(CODE_CONTROL, new net.minecraft.world.item.Item.Properties().useBlockDescriptionPrefix().setId(CODE_CONTROL_ITEM_KEY), Items.LECTERN);

	@Override
	public void onInitialize() {
		CodeProvider.REGISTRY.register(RANDOM_ID, RandomCodeProvider.CODEC);

		Registry.register(BuiltInRegistries.BLOCK, CODE_CONTROL_BLOCK_KEY, CODE_CONTROL);
		Registry.register(BuiltInRegistries.BLOCK_TYPE, CODE_CONTROL_ID, CodeControlBlock.CODEC);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, CODE_CONTROL_ID, CODE_CONTROL_BLOCK_ENTITY);
		Registry.register(BuiltInRegistries.ITEM, CODE_CONTROL_ITEM_KEY, CODE_CONTROL_ITEM);

		RegistrySyncUtils.setServerEntry(BuiltInRegistries.BLOCK_TYPE, CodeControlBlock.CODEC);
		PolymerBlockUtils.registerBlockEntity(CODE_CONTROL_BLOCK_ENTITY);
	}

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}