package hellfirepvp.modularmachinery.port.network;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MmceEnergyHatchDataPayload(
        BlockPos hatchPos,
        int containerId,
        boolean input,
        long stored,
        long capacity,
        long transferLimit
) implements CustomPacketPayload {
    public static final Type<MmceEnergyHatchDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, "energy_hatch_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MmceEnergyHatchDataPayload> STREAM_CODEC =
            CustomPacketPayload.codec(MmceEnergyHatchDataPayload::write, MmceEnergyHatchDataPayload::read);

    public MmceEnergyHatchDataPayload {
        capacity = Math.max(0L, capacity);
        stored = Math.max(0L, Math.min(stored, capacity));
        transferLimit = Math.max(0L, transferLimit);
    }

    private static MmceEnergyHatchDataPayload read(RegistryFriendlyByteBuf buffer) {
        return new MmceEnergyHatchDataPayload(
                buffer.readBlockPos(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readVarLong(),
                buffer.readVarLong(),
                buffer.readVarLong()
        );
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(hatchPos);
        buffer.writeVarInt(containerId);
        buffer.writeBoolean(input);
        buffer.writeVarLong(stored);
        buffer.writeVarLong(capacity);
        buffer.writeVarLong(transferLimit);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MmceEnergyHatchDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MmceMachineMenu machineMenu
                    && machineMenu.containerId == payload.containerId()
                    && machineMenu.blockPos().equals(payload.hatchPos())) {
                machineMenu.updateEnergyHatchData(payload);
            }
        });
    }
}
