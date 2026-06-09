package hellfirepvp.modularmachinery.port.network;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MmceControllerDataPayload(
        BlockPos controllerPos,
        int containerId,
        ResourceLocation machineId,
        ResourceLocation recipeId,
        boolean structureFormed,
        boolean working,
        int progress,
        int totalTime,
        int parallelism,
        MmceRecipeStatus status,
        String statusDetail,
        int componentCount,
        int modifierCount,
        List<String> extraInfo
) implements CustomPacketPayload {
    public static final Type<MmceControllerDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, "controller_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MmceControllerDataPayload> STREAM_CODEC =
            CustomPacketPayload.codec(MmceControllerDataPayload::write, MmceControllerDataPayload::read);

    public MmceControllerDataPayload {
        progress = Math.max(0, progress);
        totalTime = Math.max(0, totalTime);
        parallelism = Math.max(1, parallelism);
        status = status == null ? MmceRecipeStatus.IDLE : status;
        statusDetail = statusDetail == null ? "" : statusDetail;
        componentCount = Math.max(0, componentCount);
        modifierCount = Math.max(0, modifierCount);
        extraInfo = List.copyOf(extraInfo == null ? List.of() : extraInfo);
    }

    private static MmceControllerDataPayload read(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int containerId = buffer.readVarInt();
        ResourceLocation machineId = buffer.readBoolean() ? buffer.readResourceLocation() : null;
        ResourceLocation recipeId = buffer.readBoolean() ? buffer.readResourceLocation() : null;
        boolean structureFormed = buffer.readBoolean();
        boolean working = buffer.readBoolean();
        int progress = buffer.readVarInt();
        int totalTime = buffer.readVarInt();
        int parallelism = buffer.readVarInt();
        MmceRecipeStatus status = statusByOrdinal(buffer.readVarInt());
        String statusDetail = buffer.readUtf(512);
        int componentCount = buffer.readVarInt();
        int modifierCount = buffer.readVarInt();
        int extraInfoCount = buffer.readVarInt();
        java.util.ArrayList<String> extraInfo = new java.util.ArrayList<>(extraInfoCount);
        for (int index = 0; index < extraInfoCount; index++) {
            extraInfo.add(buffer.readUtf(512));
        }
        return new MmceControllerDataPayload(pos, containerId, machineId, recipeId, structureFormed, working,
                progress, totalTime, parallelism, status, statusDetail, componentCount, modifierCount, extraInfo);
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(controllerPos);
        buffer.writeVarInt(containerId);
        buffer.writeBoolean(machineId != null);
        if (machineId != null) {
            buffer.writeResourceLocation(machineId);
        }
        buffer.writeBoolean(recipeId != null);
        if (recipeId != null) {
            buffer.writeResourceLocation(recipeId);
        }
        buffer.writeBoolean(structureFormed);
        buffer.writeBoolean(working);
        buffer.writeVarInt(progress);
        buffer.writeVarInt(totalTime);
        buffer.writeVarInt(parallelism);
        buffer.writeVarInt(status.ordinal());
        buffer.writeUtf(statusDetail, 512);
        buffer.writeVarInt(componentCount);
        buffer.writeVarInt(modifierCount);
        buffer.writeVarInt(extraInfo.size());
        for (String line : extraInfo) {
            buffer.writeUtf(line == null ? "" : line, 512);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MmceControllerDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MmceMachineMenu machineMenu
                    && machineMenu.containerId == payload.containerId()
                    && machineMenu.blockPos().equals(payload.controllerPos())) {
                machineMenu.updateControllerData(payload);
            }
        });
    }

    private static MmceRecipeStatus statusByOrdinal(int ordinal) {
        MmceRecipeStatus[] values = MmceRecipeStatus.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MmceRecipeStatus.IDLE;
    }
}
