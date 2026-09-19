package net.redchujelly.cluttered.block.custom.furniture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CustomJukeboxBlock extends JukeboxBlock {
    public CustomJukeboxBlock(Properties pProperties) {
        super(pProperties);
    }

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(1,0,1, 15, 6, 15);

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack pUsedStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pState.getValue(HAS_RECORD) || pUsedStack.get(DataComponents.JUKEBOX_PLAYABLE) == null) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!pLevel.isClientSide()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (!(blockEntity instanceof JukeboxBlockEntity jukebox)) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }

            ItemStack record = pUsedStack.consumeAndReturn(1, pPlayer);
            jukebox.setTheItem(record);
            pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pPlayer, pState));
            pPlayer.awardStat(Stats.PLAY_RECORD);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        // Vanilla JukeboxBlock in this Minecraft version already registers
        // FACING itself (it's now directional). This override used to add it
        // again, which throws "duplicate property: facing" at block-state
        // init and crashes the game on startup. FACING is still available
        // to subclasses like TraditionalRadioBlock via the inherited
        // definition -- it just doesn't need to be added a second time here.
        super.createBlockStateDefinition(pBuilder);
    }
}
