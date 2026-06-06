package hellfirepvp.modularmachinery.port.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.block.ControllerBlock;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.item.MmceBlueprintData;
import hellfirepvp.modularmachinery.port.machine.MmceStructurePreview;
import hellfirepvp.modularmachinery.port.registry.MmceItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = ModularMachineryNeoForge.MODID, value = Dist.CLIENT)
public final class MmceMachineProjectionRenderer {
    private static final int MAX_RENDERED_POSITIONS = 4096;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.hitResult == null) {
            return;
        }

        HeldProjection heldProjection = heldProjection(minecraft.player);
        if (!heldProjection.enabled()) {
            return;
        }

        if (minecraft.hitResult.getType() != HitResult.Type.BLOCK || !(minecraft.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }

        BlockPos anchor = hitResult.getBlockPos();
        if (!(minecraft.level.getBlockEntity(anchor) instanceof MachineControllerBlockEntity controller)) {
            return;
        }

        Optional<ResourceLocation> targetId = heldProjection.machineId().or(controller::getMachineId);
        if (targetId.isEmpty()) {
            return;
        }

        Optional<MmceMachineDefinition> machine = MmceDataRegistry.getMachine(targetId.get());
        if (machine.isEmpty()) {
            return;
        }

        Direction facing = controllerFacing(controller);
        renderPreview(event, minecraft.level, anchor, machine.get(), facing);
    }

    private static void renderPreview(
            RenderLevelStageEvent event,
            Level level,
            BlockPos anchor,
            MmceMachineDefinition machine,
            Direction facing
    ) {
        List<MmceStructurePreview.Entry> entries = MmceStructurePreview.build(machine, facing);
        if (entries.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        VertexConsumer consumer = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        poseStack.pushPose();
        renderBox(event, poseStack, consumer, new AABB(anchor).inflate(0.006D), camera, 0.25F, 0.55F, 1.0F, 0.9F);
        int rendered = 0;
        for (MmceStructurePreview.Entry entry : entries) {
            if (rendered >= MAX_RENDERED_POSITIONS) {
                break;
            }
            BlockPos worldPos = entry.worldPos(anchor);
            AABB box = new AABB(worldPos).inflate(0.003D);
            if (!event.getFrustum().isVisible(box)) {
                continue;
            }
            boolean matches = entry.matches(level, anchor);
            if (matches) {
                renderBox(event, poseStack, consumer, box, camera, 0.15F, 1.0F, 0.25F, 0.55F);
            } else {
                renderBox(event, poseStack, consumer, box, camera, 1.0F, 0.15F, 0.15F, 0.85F);
            }
            rendered++;
        }
        minecraft.renderBuffers().bufferSource().endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private static void renderBox(
            RenderLevelStageEvent event,
            PoseStack poseStack,
            VertexConsumer consumer,
            AABB worldBox,
            Vec3 camera,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        if (!event.getFrustum().isVisible(worldBox)) {
            return;
        }
        LevelRenderer.renderLineBox(poseStack, consumer, worldBox.move(-camera.x, -camera.y, -camera.z), red, green, blue, alpha);
    }

    private static Direction controllerFacing(MachineControllerBlockEntity controller) {
        BlockState state = controller.getBlockState();
        return state.hasProperty(ControllerBlock.FACING) ? state.getValue(ControllerBlock.FACING) : Direction.NORTH;
    }

    private static HeldProjection heldProjection(Player player) {
        Optional<ResourceLocation> blueprintTarget = blueprintTarget(player.getMainHandItem())
                .or(() -> blueprintTarget(player.getOffhandItem()));
        if (blueprintTarget.isPresent()) {
            return new HeldProjection(true, blueprintTarget);
        }

        Optional<ResourceLocation> projectorTarget = projectorTarget(player.getMainHandItem())
                .or(() -> projectorTarget(player.getOffhandItem()));
        if (projectorTarget.isPresent()) {
            return new HeldProjection(true, projectorTarget);
        }

        boolean hasProjector = isProjector(player.getMainHandItem()) || isProjector(player.getOffhandItem());
        return new HeldProjection(hasProjector, Optional.empty());
    }

    private static Optional<ResourceLocation> blueprintTarget(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != MmceItems.BLUEPRINT.get()) {
            return Optional.empty();
        }
        return MmceBlueprintData.getMachineId(stack);
    }

    private static Optional<ResourceLocation> projectorTarget(ItemStack stack) {
        if (!isProjector(stack)) {
            return Optional.empty();
        }
        return MmceBlueprintData.getMachineId(stack);
    }

    private static boolean isProjector(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == MmceItems.MACHINE_PROJECTOR.get();
    }

    private record HeldProjection(boolean enabled, Optional<ResourceLocation> machineId) {
    }

    private MmceMachineProjectionRenderer() {
    }
}
