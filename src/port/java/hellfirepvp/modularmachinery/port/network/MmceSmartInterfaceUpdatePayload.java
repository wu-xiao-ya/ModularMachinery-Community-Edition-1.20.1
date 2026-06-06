package hellfirepvp.modularmachinery.port.network;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.blockentity.SmartInterfaceBlockEntity;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MmceSmartInterfaceUpdatePayload(
        BlockPos interfacePos,
        BlockPos controllerPos,
        float value
) implements CustomPacketPayload {
    public static final Type<MmceSmartInterfaceUpdatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, "smart_interface_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MmceSmartInterfaceUpdatePayload> STREAM_CODEC =
            CustomPacketPayload.codec(MmceSmartInterfaceUpdatePayload::write, MmceSmartInterfaceUpdatePayload::read);

    private static MmceSmartInterfaceUpdatePayload read(RegistryFriendlyByteBuf buffer) {
        return new MmceSmartInterfaceUpdatePayload(buffer.readBlockPos(), buffer.readBlockPos(), buffer.readFloat());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(interfacePos);
        buffer.writeBlockPos(controllerPos);
        buffer.writeFloat(value);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MmceSmartInterfaceUpdatePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.containerMenu instanceof MmceMachineMenu menu)
                || menu.kind() != MmceMachineMenu.MachineMenuKind.SMART_INTERFACE
                || !menu.blockPos().equals(payload.interfacePos())
                || !Float.isFinite(payload.value())) {
            return;
        }
        if (!player.canInteractWithBlock(payload.interfacePos(), 4.0)) {
            return;
        }
        BlockEntity blockEntity = player.level().getBlockEntity(payload.interfacePos());
        if (blockEntity instanceof SmartInterfaceBlockEntity smartInterface
                && smartInterface.updateValue(payload.controllerPos(), payload.value())) {
            menu.broadcastChanges();
        }
    }
}
