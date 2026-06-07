package hellfirepvp.modularmachinery.port.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BaseMachineBlockEntity extends BlockEntity {
    public static final int DEFAULT_MACHINE_COLOR = 0xFFFFFF;

    private int machineColor = DEFAULT_MACHINE_COLOR;
    private int groupId = -1;
    private boolean groupInput;

    public BaseMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int getMachineColor() {
        return machineColor;
    }

    public void setMachineColor(int machineColor) {
        if (this.machineColor != machineColor) {
            this.machineColor = machineColor;
            markForSync();
        }
    }

    public int getGroupId() {
        return groupInput ? getConfiguredGroupId() : -1;
    }

    public void setGroupId(int groupId) {
        int normalized = Math.max(0, groupId);
        if (this.groupId != normalized) {
            this.groupId = normalized;
            markForSync();
        }
    }

    public boolean isGroupInput() {
        return groupInput;
    }

    public int getConfiguredGroupId() {
        return Math.max(0, groupId);
    }

    public void setGroupInput(boolean groupInput) {
        if (this.groupInput != groupInput) {
            this.groupInput = groupInput;
            markForSync();
        }
    }

    public boolean canConfigureGroupInput() {
        return false;
    }

    public void markForSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        machineColor = tag.contains("casingColor") ? tag.getInt("casingColor") : DEFAULT_MACHINE_COLOR;
        groupId = tag.getInt("groupId");
        groupInput = tag.getBoolean("isGroupInput");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("casingColor", machineColor);
        tag.putInt("groupId", groupId);
        tag.putBoolean("isGroupInput", groupInput);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
