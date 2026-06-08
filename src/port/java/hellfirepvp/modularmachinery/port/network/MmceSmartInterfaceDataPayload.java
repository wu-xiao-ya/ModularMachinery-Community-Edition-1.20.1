package hellfirepvp.modularmachinery.port.network;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MmceSmartInterfaceDataPayload(
        BlockPos interfacePos,
        int containerId,
        List<BindingDetail> bindings
) implements CustomPacketPayload {
    public static final Type<MmceSmartInterfaceDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, "smart_interface_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MmceSmartInterfaceDataPayload> STREAM_CODEC =
            CustomPacketPayload.codec(MmceSmartInterfaceDataPayload::write, MmceSmartInterfaceDataPayload::read);

    public MmceSmartInterfaceDataPayload {
        bindings = List.copyOf(bindings == null ? List.of() : bindings);
    }

    private static MmceSmartInterfaceDataPayload read(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int containerId = buffer.readVarInt();
        int bindingCount = buffer.readVarInt();
        List<BindingDetail> bindings = new ArrayList<>(bindingCount);
        for (int index = 0; index < bindingCount; index++) {
            bindings.add(readBinding(buffer));
        }
        return new MmceSmartInterfaceDataPayload(pos, containerId, bindings);
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(interfacePos);
        buffer.writeVarInt(containerId);
        buffer.writeVarInt(bindings.size());
        for (BindingDetail binding : bindings) {
            writeBinding(buffer, binding);
        }
    }

    private static BindingDetail readBinding(RegistryFriendlyByteBuf buffer) {
        BlockPos controllerPos = buffer.readBlockPos();
        ResourceLocation machineId = buffer.readBoolean() ? buffer.readResourceLocation() : null;
        String machineName = buffer.readUtf(128);
        String type = buffer.readUtf(128);
        float value = buffer.readFloat();
        boolean controllerPresent = buffer.readBoolean();
        boolean structureFormed = buffer.readBoolean();
        boolean working = buffer.readBoolean();
        MmceRecipeStatus status = statusByOrdinal(buffer.readVarInt());
        String statusDetail = buffer.readUtf(512);
        return new BindingDetail(
                controllerPos,
                machineId,
                machineName,
                type,
                value,
                controllerPresent,
                structureFormed,
                working,
                status,
                statusDetail
        );
    }

    private static void writeBinding(RegistryFriendlyByteBuf buffer, BindingDetail binding) {
        buffer.writeBlockPos(binding.controllerPos());
        buffer.writeBoolean(binding.machineId() != null);
        if (binding.machineId() != null) {
            buffer.writeResourceLocation(binding.machineId());
        }
        buffer.writeUtf(binding.machineName(), 128);
        buffer.writeUtf(binding.type(), 128);
        buffer.writeFloat(binding.value());
        buffer.writeBoolean(binding.controllerPresent());
        buffer.writeBoolean(binding.structureFormed());
        buffer.writeBoolean(binding.working());
        buffer.writeVarInt(binding.status().ordinal());
        buffer.writeUtf(binding.statusDetail(), 512);
    }

    private static MmceRecipeStatus statusByOrdinal(int ordinal) {
        MmceRecipeStatus[] values = MmceRecipeStatus.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MmceRecipeStatus.IDLE;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MmceSmartInterfaceDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MmceMachineMenu menu
                    && menu.containerId == payload.containerId()
                    && menu.blockPos().equals(payload.interfacePos())) {
                menu.updateSmartInterfaceData(payload);
            }
        });
    }

    public record BindingDetail(
            BlockPos controllerPos,
            ResourceLocation machineId,
            String machineName,
            String type,
            float value,
            boolean controllerPresent,
            boolean structureFormed,
            boolean working,
            MmceRecipeStatus status,
            String statusDetail
    ) {
        public BindingDetail {
            machineName = machineName == null ? "" : machineName;
            type = type == null ? "" : type;
            status = status == null ? MmceRecipeStatus.IDLE : status;
            statusDetail = statusDetail == null ? "" : statusDetail;
        }
    }
}
