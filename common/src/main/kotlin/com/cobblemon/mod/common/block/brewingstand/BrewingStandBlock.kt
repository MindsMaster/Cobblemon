package com.cobblemon.mod.common.block.brewingstand

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.block.entity.BrewingStandBlockEntity
import com.mojang.serialization.MapCodec
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.stats.Stats
import net.minecraft.util.RandomSource
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

@Environment(EnvType.CLIENT)
class BrewingStandBlock(settings: Properties) : BaseEntityBlock(settings) {
    companion object {
        val CODEC = simpleCodec(::BrewingStandBlock)
        val HAS_BOTTLE = arrayOf(
            BlockStateProperties.HAS_BOTTLE_0,
            BlockStateProperties.HAS_BOTTLE_1,
            BlockStateProperties.HAS_BOTTLE_2,
        )
        private val AABB = Shapes.or(
            Shapes.box(0.0625, 0.0, 0.0625, 0.9375, 0.125, 0.9375),
            Shapes.box(0.4375, 0.0, 0.4375, 0.5625, 0.875, 0.5625)
        )

    }

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HAS_BOTTLE[0], false)
                .setValue(HAS_BOTTLE[1], false)
                .setValue(HAS_BOTTLE[2], false)
        )
    }

    override fun getShape(blockState: BlockState, blockGetter: BlockGetter, blockPos: BlockPos, collisionContext: CollisionContext): VoxelShape = AABB

    override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        AABB


    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun <T : BlockEntity?> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T?>
    ): BlockEntityTicker<T?>? {
        return if (level.isClientSide) null else createTickerHelper<BrewingStandBlockEntity?, T?>(
            blockEntityType,
            CobblemonBlockEntities.BREWING_STAND,
            BlockEntityTicker { level: Level, pos: BlockPos, state: BlockState, blockEntity: BrewingStandBlockEntity? ->
                blockEntity?.serverTick(level, pos, state, blockEntity)
            })
    }

    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        movedByPiston: Boolean
    ) {
        Containers.dropContentsOnDestroy(state, newState, level, pos)
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS
        } else {
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is BrewingStandBlockEntity) {
                player.openMenu(blockEntity)
                player.awardStat(Stats.INTERACT_WITH_BREWINGSTAND)
            }

            return InteractionResult.CONSUME
        }
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        val d = pos.getX().toDouble() + 0.4 + random.nextFloat().toDouble() * 0.2
        val e = pos.getY().toDouble() + 0.7 + random.nextFloat().toDouble() * 0.3
        val f = pos.getZ().toDouble() + 0.4 + random.nextFloat().toDouble() * 0.2
        level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0)
    }

    override fun hasAnalogOutputSignal(state: BlockState): Boolean {
        return true
    }

    override fun getAnalogOutputSignal(state: BlockState, level: Level, pos: BlockPos): Int {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos))
    }

    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType): Boolean {
        return false
    }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        return if (direction == state.getValue(FACING) && !state.canSurvive(world, pos)) Blocks.AIR.defaultBlockState()
        else super.updateShape(state, direction, neighborState, world, pos, neighborPos)
    }


    override fun codec(): MapCodec<out BaseEntityBlock?>? {
        return CODEC
    }

    override fun newBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity? {
        return BrewingStandBlockEntity(pos, state)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        builder.add(
            FACING,
            *arrayOf<Property<*>?>(
                HAS_BOTTLE[0],
                HAS_BOTTLE[1],
                HAS_BOTTLE[2]
            )
        )
    }

    override fun getCloneItemStack(level: LevelReader, pos: BlockPos, state: BlockState): ItemStack? {
        return ItemStack(Blocks.BREWING_STAND)
    }
}