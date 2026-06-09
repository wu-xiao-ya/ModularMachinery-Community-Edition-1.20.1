package hellfirepvp.modularmachinery.port.network;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MmceFluidHatchDataPayload(
        BlockPos hatchPos,
        int containerId,
        ResourceLocation fluidId,
        int fluidAmount,
        ResourceLocation chemicalId,
        int chemicalAmount,
        int capacity
) implements CustomPacketPayload {
    public static final Type<MmceFluidHatchDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, "fluid_hatch_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MmceFluidHatchDataPayload> STREAM_CODEC =
            CustomPacketPayload.codec(MmceFluidHatchDataPayload::write, MmceFluidHatchDataPayload::read);

    public MmceFluidHatchDataPayload {
        capacity = Math.max(0, capacity);
        fluidAmount = Math.max(0, Math.min(fluidAmount, capacity));
        fluidId = fluidAmount <= 0 ? null : fluidId;
        chemicalAmount = Math.max(0, Math.min(chemicalAmount, capacity));
        chemicalId = chemicalAmount <= 0 ? null : chemicalId;
    }

    private static MmceFluidHatchDataPayload read(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int containerId = buffer.readVarInt();
        ResourceLocation fluidId = buffer.readBoolean() ? buffer.readResourceLocation() : null;
        int fluidAmount = buffer.readVarInt();
        ResourceLocation chemicalId = buffer.readBoolean() ? buffer.readResourceLocation() : null;
        int chemicalAmount = buffer.readVarInt();
        int capacity = buffer.readVarInt();
        return new MmceFluidHatchDataPayload(pos, containerId, fluidId, fluidAmount, chemicalId, chemicalAmount, capacity);
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(hatchPos);
        buffer.writeVarInt(containerId);
        buffer.writeBoolean(fluidId != null);
        if (fluidId != null) {
            buffer.writeResourceLocation(fluidId);
        }
        buffer.writeVarInt(fluidAmount);
        buffer.writeBoolean(chemicalId != null);
        if (chemicalId != null) {
            buffer.writeResourceLocation(chemicalId);
        }
        buffer.writeVarInt(chemicalAmount);
        buffer.writeVarInt(capacity);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MmceFluidHatchDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MmceMachineMenu machineMenu
                    && machineMenu.containerId == payload.containerId()
                    && machineMenu.blockPos().equals(payload.hatchPos())) {
                machineMenu.updateFluidHatchData(payload);
            }
        });
    }
}
