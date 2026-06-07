package hellfirepvp.modularmachinery.port.network;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.blockentity.FluidHatchBlockEntity;
import hellfirepvp.modularmachinery.port.capability.MmceFluidHandler;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MmceFluidGuiInteractPayload(BlockPos hatchPos) implements CustomPacketPayload {
    public static final Type<MmceFluidGuiInteractPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, "fluid_gui_interact"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MmceFluidGuiInteractPayload> STREAM_CODEC =
            CustomPacketPayload.codec(MmceFluidGuiInteractPayload::write, MmceFluidGuiInteractPayload::read);

    private static MmceFluidGuiInteractPayload read(RegistryFriendlyByteBuf buffer) {
        return new MmceFluidGuiInteractPayload(buffer.readBlockPos());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(hatchPos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MmceFluidGuiInteractPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.containerMenu instanceof MmceMachineMenu menu)
                || !menu.blockPos().equals(payload.hatchPos())
                || !isFluidMenu(menu.kind())
                || !player.canInteractWithBlock(payload.hatchPos(), 4.0)) {
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(payload.hatchPos());
        if (!(blockEntity instanceof FluidHatchBlockEntity hatch)) {
            return;
        }

        ItemStack carried = menu.getCarried();
        if (carried.isEmpty() || FluidUtil.getFluidHandler(carried).isEmpty()) {
            return;
        }

        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        if (inventory == null) {
            return;
        }

        MmceFluidHandler handler = new MmceFluidHandler(hatch);
        FluidActionResult result = FluidUtil.tryFillContainerAndStow(carried, handler, inventory, Integer.MAX_VALUE, player, true);
        if (!result.isSuccess()) {
            result = FluidUtil.tryEmptyContainerAndStow(carried, handler, inventory, Integer.MAX_VALUE, player, true);
        }
        if (result.isSuccess()) {
            menu.setCarried(result.getResult());
            menu.broadcastChanges();
        }
    }

    private static boolean isFluidMenu(MmceMachineMenu.MachineMenuKind kind) {
        return switch (kind) {
            case FLUID_INPUT_HATCH, FLUID_OUTPUT_HATCH, FLUID_PROCESSOR_HATCH -> true;
            default -> false;
        };
    }
}
