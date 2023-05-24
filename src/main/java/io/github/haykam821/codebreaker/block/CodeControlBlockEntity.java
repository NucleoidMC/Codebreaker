package io.github.haykam821.codebreaker.block;

import org.joml.Matrix4x3f;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import io.github.haykam821.codebreaker.Main;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class CodeControlBlockEntity extends BlockEntity {
	protected static final String BLOCK_KEY = "block";

	private BlockState block = Blocks.AIR.getDefaultState();

	private BlockDisplayElement element;
	private ElementHolder holder;

	public CodeControlBlockEntity(BlockPos pos, BlockState state) {
		super(Main.CODE_CONTROL_BLOCK_ENTITY, pos, state);
	}

	public BlockState getBlock() {
		return this.block;
	}

	public void setBlock(BlockState block) {
		this.block = block;
		this.markDirty();
	}

	@Override
	public void markDirty() {
		super.markDirty();

		if (this.element != null) {
			this.element.setBlockState(this.block);
		}
	}

	@Override
	public void markRemoved() {
		if (this.holder != null) {
			this.holder.destroy();
		}
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);

		RegistryEntryLookup<Block> registryWrapper = this.world == null ? Registries.BLOCK.getReadOnlyWrapper() : this.world.createCommandRegistryWrapper(RegistryKeys.BLOCK);
		this.block = NbtHelper.toBlockState(registryWrapper, nbt.getCompound(BLOCK_KEY));
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.put(BLOCK_KEY, NbtHelper.fromBlockState(this.block));
	}

	public static void tick(World world, BlockPos pos, BlockState state, CodeControlBlockEntity blockEntity) {
		if (blockEntity.holder == null) {
			Matrix4x3f matrix = new Matrix4x3f();

			Direction facing = state.get(CodeControlBlock.FACING);
			matrix.rotate(facing.getRotationQuaternion());

			matrix.rotateX(MathHelper.RADIANS_PER_DEGREE * 22.5f);
			matrix.scale(0.5f, 0.5f, 0.25f);
			matrix.translate(0f, -0.2f, -1.55f);
			matrix.rotateX((float) Math.PI);
			matrix.translate(-0.5f, -0.5f, -0.5f);

			blockEntity.element = new BlockDisplayElement(blockEntity.getBlock());
			blockEntity.element.setTransformation(matrix);

			blockEntity.holder = new ElementHolder();
			blockEntity.holder.addElement(blockEntity.element);

			BlockBoundAttachment.ofTicking(blockEntity.holder, (ServerWorld) world, pos);
		}
	}
}
