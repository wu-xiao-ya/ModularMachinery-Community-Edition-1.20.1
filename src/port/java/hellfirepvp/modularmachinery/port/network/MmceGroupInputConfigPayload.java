package hellfirepvp.modularmachinery.port.network;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.blockentity.BaseMachineBlockEntity;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MmceGroupInputConfigPayload(BlockPos pos, int groupId, boolean groupInput) implements CustomPacketPayload {
    public static final Type<MmceGroupInputConfigPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, "group_input_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MmceGroupInputConfigPayload> STREAM_CODEC =
            CustomPacketPayload.codec(MmceGroupInputConfigPayload::write, MmceGroupInputConfigPayload::read);

    private static MmceGroupInputConfigPayload read(RegistryFriendlyByteBuf buffer) {
        return new MmceGroupInputConfigPayload(buffer.readBlockPos(), buffer.readVarInt(), buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(groupId);
        buffer.writeBoolean(groupInput);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MmceGroupInputConfigPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.containerMenu instanceof MmceMachineMenu menu)
                || !menu.blockPos().equals(payload.pos())
                || !player.canInteractWithBlock(payload.pos(), 4.0)) {
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(payload.pos());
        if (blockEntity instanceof BaseMachineBlockEntity machineBlockEntity
                && machineBlockEntity.canConfigureGroupInput()) {
            machineBlockEntity.setGroupId(payload.groupId());
            machineBlockEntity.setGroupInput(payload.groupInput());
            menu.broadcastChanges();
        }
    }
}
