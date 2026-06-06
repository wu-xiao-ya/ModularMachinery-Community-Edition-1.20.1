package hellfirepvp.modularmachinery.port.network;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.blockentity.FactoryControllerBlockEntity;
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

public record MmceFactoryRunsPayload(
        BlockPos controllerPos,
        int containerId,
        List<FactoryControllerBlockEntity.FactoryRunView> runs,
        int activeRuns,
        int workingRuns,
        int regularActiveRuns,
        int maxThreads,
        int totalParallelism
) implements CustomPacketPayload {
    public static final Type<MmceFactoryRunsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, "factory_runs"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MmceFactoryRunsPayload> STREAM_CODEC =
            CustomPacketPayload.codec(MmceFactoryRunsPayload::write, MmceFactoryRunsPayload::read);

    public MmceFactoryRunsPayload {
        runs = List.copyOf(runs == null ? List.of() : runs);
        activeRuns = Math.max(0, activeRuns);
        workingRuns = Math.max(0, workingRuns);
        regularActiveRuns = Math.max(0, regularActiveRuns);
        maxThreads = Math.max(0, maxThreads);
        totalParallelism = Math.max(0, totalParallelism);
    }

    private static MmceFactoryRunsPayload read(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int containerId = buffer.readVarInt();
        int runCount = buffer.readVarInt();
        List<FactoryControllerBlockEntity.FactoryRunView> runs = new ArrayList<>(runCount);
        for (int index = 0; index < runCount; index++) {
            runs.add(readRun(buffer));
        }
        return new MmceFactoryRunsPayload(
                pos,
                containerId,
                runs,
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(controllerPos);
        buffer.writeVarInt(containerId);
        buffer.writeVarInt(runs.size());
        for (FactoryControllerBlockEntity.FactoryRunView run : runs) {
            writeRun(buffer, run);
        }
        buffer.writeVarInt(activeRuns);
        buffer.writeVarInt(workingRuns);
        buffer.writeVarInt(regularActiveRuns);
        buffer.writeVarInt(maxThreads);
        buffer.writeVarInt(totalParallelism);
    }

    private static FactoryControllerBlockEntity.FactoryRunView readRun(RegistryFriendlyByteBuf buffer) {
        boolean coreThread = buffer.readBoolean();
        String threadName = buffer.readUtf(128);
        ResourceLocation recipeId = buffer.readBoolean() ? buffer.readResourceLocation() : null;
        int progress = buffer.readVarInt();
        int totalTime = buffer.readVarInt();
        int parallelism = buffer.readVarInt();
        boolean working = buffer.readBoolean();
        MmceRecipeStatus status = statusByOrdinal(buffer.readVarInt());
        String detail = buffer.readUtf(512);
        return new FactoryControllerBlockEntity.FactoryRunView(
                coreThread,
                threadName,
                recipeId,
                progress,
                totalTime,
                parallelism,
                working,
                status,
                detail
        );
    }

    private static void writeRun(RegistryFriendlyByteBuf buffer, FactoryControllerBlockEntity.FactoryRunView run) {
        buffer.writeBoolean(run.coreThread());
        buffer.writeUtf(run.threadName(), 128);
        buffer.writeBoolean(run.activeRecipeId() != null);
        if (run.activeRecipeId() != null) {
            buffer.writeResourceLocation(run.activeRecipeId());
        }
        buffer.writeVarInt(run.progress());
        buffer.writeVarInt(run.totalTime());
        buffer.writeVarInt(run.parallelism());
        buffer.writeBoolean(run.working());
        buffer.writeVarInt(run.status().ordinal());
        buffer.writeUtf(run.detail(), 512);
    }

    private static MmceRecipeStatus statusByOrdinal(int ordinal) {
        MmceRecipeStatus[] values = MmceRecipeStatus.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MmceRecipeStatus.IDLE;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MmceFactoryRunsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MmceMachineMenu machineMenu
                    && machineMenu.containerId == payload.containerId()
                    && machineMenu.blockPos().equals(payload.controllerPos())) {
                machineMenu.updateFactoryRuns(payload);
            }
        });
    }
}
