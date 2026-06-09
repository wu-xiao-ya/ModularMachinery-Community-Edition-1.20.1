package hellfirepvp.modularmachinery.port.menu;

import hellfirepvp.modularmachinery.port.blockentity.BaseMachineBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.EnergyHatchBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.FactoryControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.FluidHatchBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.ItemBusBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.ParallelControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.SmartInterfaceBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.UpgradeBusBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceRecipeDefinition;
import hellfirepvp.modularmachinery.port.event.MmceControllerButtonClickEvent;
import hellfirepvp.modularmachinery.port.event.MmceControllerGUIRenderEvent;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgrade;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeRegistry;
import hellfirepvp.modularmachinery.port.machine.MmceStructureMatcher;
import hellfirepvp.modularmachinery.port.network.MmceEnergyHatchDataPayload;
import hellfirepvp.modularmachinery.port.network.MmceFactoryRunsPayload;
import hellfirepvp.modularmachinery.port.network.MmceFluidHatchDataPayload;
import hellfirepvp.modularmachinery.port.network.MmceSmartInterfaceDataPayload;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import hellfirepvp.modularmachinery.port.registry.MmceMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class MmceMachineMenu extends AbstractContainerMenu {
    public static final int BUTTON_REFRESH_STRUCTURE = 0;
    public static final int BUTTON_PARALLEL_DECREMENT_1 = 1;
    public static final int BUTTON_PARALLEL_DECREMENT_10 = 2;
    public static final int BUTTON_PARALLEL_DECREMENT_100 = 3;
    public static final int BUTTON_PARALLEL_INCREMENT_1 = 4;
    public static final int BUTTON_PARALLEL_INCREMENT_10 = 5;
    public static final int BUTTON_PARALLEL_INCREMENT_100 = 6;
    public static final int BUTTON_PARALLEL_SET_BASE = 1_000;

    private static final int DATA_COUNT = 14;
    private static final int DATA_KIND = 0;
    private static final int DATA_COLOR = 1;
    private static final int DATA_GROUP_ID = 2;
    private static final int DATA_GROUP_INPUT = 3;
    private static final int DATA_A = 4;
    private static final int DATA_B = 5;
    private static final int DATA_C = 6;
    private static final int DATA_D = 7;
    private static final int DATA_E = 8;
    private static final int DATA_F = 9;
    private static final int DATA_G = 10;
    private static final int DATA_H = 11;
    private static final int DATA_CONFIGURED_GROUP_ID = 12;
    private static final int DATA_CAN_CONFIGURE_GROUP = 13;

    private static final int DEFAULT_IMAGE_WIDTH = 176;
    private static final int DEFAULT_IMAGE_HEIGHT = 166;
    private static final int LARGE_IMAGE_HEIGHT = 213;
    private static final int FACTORY_IMAGE_WIDTH = 280;

    private final Inventory playerInventory;
    private final BlockPos blockPos;
    private final BaseMachineBlockEntity blockEntity;
    private final Container machineContainer;
    private final int machineSlotCount;
    private final MachineMenuKind fallbackKind;
    private final int playerInventoryX;
    private final int playerInventoryY;
    private final int imageWidth;
    private final int imageHeight;
    private final int[] clientData = new int[DATA_COUNT];
    private final boolean clientSide;
    private List<FactoryControllerBlockEntity.FactoryRunView> factoryRuns = List.of();
    private int factoryActiveRuns;
    private int factoryWorkingRuns;
    private int factoryRegularActiveRuns;
    private int factoryMaxThreads;
    private int factoryTotalParallelism;
    private String lastFactorySignature = "";
    private boolean clientEnergyHatchDataReceived;
    private boolean clientEnergyInput;
    private long clientEnergyStored;
    private long clientEnergyCapacity;
    private long clientEnergyTransferLimit;
    private String lastEnergyHatchSignature = "";
    private ResourceLocation clientFluidId;
    private int clientFluidAmount;
    private int clientFluidCapacity = 1;
    private ResourceLocation clientChemicalId;
    private int clientChemicalAmount;
    private boolean clientFluidHatchDataReceived;
    private String lastFluidHatchSignature = "";
    private List<MmceSmartInterfaceDataPayload.BindingDetail> smartInterfaceBindings = List.of();
    private String lastSmartInterfaceSignature = "";

    public static MmceMachineMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        BlockPos pos = data == null ? BlockPos.ZERO : data.readBlockPos();
        int fallbackSlots = data == null ? 0 : data.readVarInt();
        MachineMenuKind fallbackKind = data != null && data.readableBytes() > 0
                ? MachineMenuKind.byOrdinal(data.readVarInt())
                : MachineMenuKind.UNKNOWN;
        Level level = playerInventory.player.level();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        BaseMachineBlockEntity machineBlockEntity = blockEntity instanceof BaseMachineBlockEntity machine ? machine : null;
        return new MmceMachineMenu(containerId, playerInventory, pos, machineBlockEntity, fallbackSlots, fallbackKind);
    }

    public MmceMachineMenu(int containerId, Inventory playerInventory, BaseMachineBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity.getBlockPos(), blockEntity, slotCount(blockEntity), kindFor(blockEntity));
    }

    private MmceMachineMenu(int containerId, Inventory playerInventory, BlockPos blockPos,
                            BaseMachineBlockEntity blockEntity, int fallbackSlots, MachineMenuKind fallbackKind) {
        super(MmceMenus.MACHINE.get(), containerId);
        this.playerInventory = playerInventory;
        this.blockPos = blockPos;
        this.blockEntity = blockEntity;
        this.clientSide = playerInventory.player.level().isClientSide();
        this.fallbackKind = blockEntity == null ? fallbackKind : kindFor(blockEntity);
        this.machineContainer = blockEntity instanceof Container container
                ? container
                : new SimpleContainer(Math.max(0, fallbackSlots));
        this.machineSlotCount = machineContainer.getContainerSize();
        this.imageWidth = imageWidthFor(this.fallbackKind);
        this.imageHeight = imageHeightFor(this.fallbackKind);
        this.playerInventoryX = playerInventoryXFor(this.fallbackKind);
        this.playerInventoryY = playerInventoryYFor(this.fallbackKind);

        addMachineSlots();
        addPlayerInventorySlots();
        addSynchronizedData();
    }

    public static net.minecraft.world.InteractionResult open(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof BaseMachineBlockEntity blockEntity)) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, menuPlayer) -> new MmceMachineMenu(containerId, inventory, blockEntity),
                    titleFor(blockEntity)
            ), buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeVarInt(slotCount(blockEntity));
                buffer.writeVarInt(kindFor(blockEntity).ordinal());
            });
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide());
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public int machineSlotCount() {
        return machineSlotCount;
    }

    public int playerInventoryY() {
        return playerInventoryY;
    }

    public int playerInventoryX() {
        return playerInventoryX;
    }

    public int imageWidth() {
        return imageWidth;
    }

    public int imageHeight() {
        return imageHeight;
    }

    public MachineMenuKind kind() {
        MachineMenuKind synchronizedKind = MachineMenuKind.byOrdinal(data(DATA_KIND));
        return synchronizedKind == MachineMenuKind.UNKNOWN ? fallbackKind : synchronizedKind;
    }

    public boolean canRefreshStructure() {
        MachineMenuKind menuKind = kind();
        return menuKind == MachineMenuKind.CONTROLLER || menuKind == MachineMenuKind.FACTORY_CONTROLLER;
    }

    public int parallelism() {
        return data(DATA_A);
    }

    public int maxParallelism() {
        return data(DATA_B);
    }

    public int machineColor() {
        return data(DATA_COLOR) & 0xFFFFFF;
    }

    public boolean canConfigureGroupInput() {
        return data(DATA_CAN_CONFIGURE_GROUP) == 1;
    }

    public int configuredGroupId() {
        return Math.max(0, data(DATA_CONFIGURED_GROUP_ID));
    }

    public boolean groupInputEnabled() {
        return data(DATA_GROUP_INPUT) == 1;
    }

    public boolean structureFormed() {
        return data(DATA_A) == 1;
    }

    public boolean working() {
        return data(DATA_B) == 1;
    }

    public int recipeProgress() {
        return data(DATA_C);
    }

    public int recipeTotalTime() {
        return Math.max(1, data(DATA_H));
    }

    public long energyStored() {
        if (!clientSide && blockEntity instanceof EnergyHatchBlockEntity hatch) {
            return hatch.getEnergy();
        }
        if (clientEnergyHatchDataReceived) {
            return clientEnergyStored;
        }
        return unsignedLong(DATA_B, DATA_C);
    }

    public long energyCapacity() {
        if (!clientSide && blockEntity instanceof EnergyHatchBlockEntity hatch) {
            return hatch.getCapacity();
        }
        if (clientEnergyHatchDataReceived) {
            return Math.max(1L, clientEnergyCapacity);
        }
        return unsignedLong(DATA_D, DATA_E);
    }

    public long energyTransferLimit() {
        if (!clientSide && blockEntity instanceof EnergyHatchBlockEntity hatch) {
            return hatch.getTransferLimit();
        }
        if (clientEnergyHatchDataReceived) {
            return clientEnergyTransferLimit;
        }
        return unsignedLong(DATA_F, DATA_G);
    }

    public boolean energyInput() {
        if (!clientSide && blockEntity instanceof EnergyHatchBlockEntity hatch) {
            return hatch.isInput();
        }
        if (clientEnergyHatchDataReceived) {
            return clientEnergyInput;
        }
        return data(DATA_A) == 1;
    }

    public int fluidStored() {
        if (!clientSide && blockEntity instanceof FluidHatchBlockEntity hatch) {
            return hatch.getStoredFluid().getAmount();
        }
        if (clientFluidHatchDataReceived) {
            return clientFluidAmount;
        }
        return data(DATA_C);
    }

    public int fluidCapacity() {
        if (!clientSide && blockEntity instanceof FluidHatchBlockEntity hatch) {
            return Math.max(1, hatch.getCapacity());
        }
        if (clientFluidHatchDataReceived) {
            return Math.max(1, clientFluidCapacity);
        }
        return Math.max(1, data(DATA_D));
    }

    public ResourceLocation storedFluidId() {
        if (!clientSide && blockEntity instanceof FluidHatchBlockEntity hatch) {
            FluidStack fluid = hatch.getStoredFluid();
            return fluid.isEmpty() ? null : BuiltInRegistries.FLUID.getKey(fluid.getFluid());
        }
        return clientFluidHatchDataReceived ? clientFluidId : null;
    }

    public boolean hasStoredChemical() {
        if (!clientSide && blockEntity instanceof FluidHatchBlockEntity hatch) {
            return hatch.hasStoredChemical();
        }
        if (clientFluidHatchDataReceived) {
            return clientChemicalId != null && clientChemicalAmount > 0;
        }
        return data(DATA_F) == 1 && data(DATA_E) > 0;
    }

    public ResourceLocation storedChemicalId() {
        if (blockEntity instanceof FluidHatchBlockEntity hatch && hatch.hasStoredChemical()) {
            return hatch.getStoredChemicalId();
        }
        return clientFluidHatchDataReceived ? clientChemicalId : null;
    }

    public int storedChemicalAmount() {
        if (!clientSide && blockEntity instanceof FluidHatchBlockEntity hatch) {
            return hatch.getStoredChemicalAmount();
        }
        return clientFluidHatchDataReceived ? clientChemicalAmount : Math.max(0, data(DATA_E));
    }

    public String storedChemicalName() {
        ResourceLocation chemicalId = storedChemicalId();
        return chemicalId == null ? "unknown" : prettifyIdPath(chemicalId);
    }

    public String storedChemicalIdText() {
        ResourceLocation chemicalId = storedChemicalId();
        return chemicalId == null ? "unknown" : chemicalId.toString();
    }

    public List<MmceSmartInterfaceDataPayload.BindingDetail> smartInterfaceBindings() {
        if (!clientSide && blockEntity instanceof SmartInterfaceBlockEntity smartInterface) {
            return smartInterfaceBindingDetails(smartInterface);
        }
        return smartInterfaceBindings;
    }

    public MmceSmartInterfaceDataPayload.BindingDetail smartInterfaceBinding(int index) {
        List<MmceSmartInterfaceDataPayload.BindingDetail> bindings = smartInterfaceBindings();
        return index >= 0 && index < bindings.size() ? bindings.get(index) : null;
    }

    public List<FactoryControllerBlockEntity.FactoryRunView> factoryRuns() {
        if (!clientSide && blockEntity instanceof FactoryControllerBlockEntity factory) {
            return factory.factoryRunViews();
        }
        return factoryRuns;
    }

    public int factoryActiveRuns() {
        return clientSide ? factoryActiveRuns : blockEntity instanceof FactoryControllerBlockEntity factory ? factory.factoryActiveRunCount() : 0;
    }

    public int factoryWorkingRuns() {
        return clientSide ? factoryWorkingRuns : blockEntity instanceof FactoryControllerBlockEntity factory ? factory.factoryWorkingRunCount() : 0;
    }

    public int factoryRegularActiveRuns() {
        return clientSide ? factoryRegularActiveRuns : blockEntity instanceof FactoryControllerBlockEntity factory ? factory.factoryRegularActiveRunCount() : 0;
    }

    public int factoryMaxThreads() {
        return clientSide ? factoryMaxThreads : blockEntity instanceof FactoryControllerBlockEntity factory ? factory.factoryMaxThreads() : 0;
    }

    public int factoryTotalParallelism() {
        return clientSide ? factoryTotalParallelism : data(DATA_D);
    }

    public void updateFactoryRuns(MmceFactoryRunsPayload payload) {
        factoryRuns = payload.runs();
        factoryActiveRuns = payload.activeRuns();
        factoryWorkingRuns = payload.workingRuns();
        factoryRegularActiveRuns = payload.regularActiveRuns();
        factoryMaxThreads = payload.maxThreads();
        factoryTotalParallelism = payload.totalParallelism();
    }

    public void updateSmartInterfaceData(MmceSmartInterfaceDataPayload payload) {
        smartInterfaceBindings = payload.bindings();
    }

    public void updateEnergyHatchData(MmceEnergyHatchDataPayload payload) {
        clientEnergyInput = payload.input();
        clientEnergyStored = payload.stored();
        clientEnergyCapacity = payload.capacity();
        clientEnergyTransferLimit = payload.transferLimit();
        clientEnergyHatchDataReceived = true;
    }

    public void updateFluidHatchData(MmceFluidHatchDataPayload payload) {
        clientFluidId = payload.fluidId();
        clientFluidAmount = payload.fluidAmount();
        clientFluidCapacity = payload.capacity();
        clientChemicalId = payload.chemicalId();
        clientChemicalAmount = payload.chemicalAmount();
        clientFluidHatchDataReceived = true;
    }

    public List<Component> statusLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("Position: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ()));
        switch (kind()) {
            case CONTROLLER -> addControllerLines(lines);
            case FACTORY_CONTROLLER -> addFactoryControllerLines(lines);
            case ITEM_INPUT_BUS, ITEM_OUTPUT_BUS -> {
                lines.add(Component.literal(data(DATA_A) == 1 ? "Mode: Item input" : "Mode: Item output"));
                lines.add(Component.literal("Slots: " + data(DATA_B)));
            }
            case UPGRADE_BUS -> addUpgradeBusLines(lines);
            case ENERGY_INPUT_HATCH, ENERGY_OUTPUT_HATCH -> addEnergyLines(lines);
            case FLUID_INPUT_HATCH, FLUID_OUTPUT_HATCH, FLUID_PROCESSOR_HATCH -> addFluidLines(lines);
            case SMART_INTERFACE -> {
                lines.add(Component.literal("Mode: Smart interface"));
                lines.add(Component.literal("Bindings: " + data(DATA_B)));
            }
            case PARALLEL_CONTROLLER -> {
                lines.add(Component.literal("Max parallelism: " + data(DATA_B)));
                lines.add(Component.literal("Current parallelism: " + data(DATA_A)));
            }
            default -> lines.add(Component.literal("Component: " + kind().displayName()));
        }
        if (canConfigureGroupInput()) {
            lines.add(Component.literal("Group input: " + (groupInputEnabled() ? "enabled" : "disabled")
                    + " | ID: " + configuredGroupId()));
        } else {
            int groupId = data(DATA_GROUP_ID);
            if (groupId >= 0) {
                lines.add(Component.literal("Group: " + groupId + (data(DATA_GROUP_INPUT) == 1 ? " input" : " output")));
            }
        }
        lines.add(Component.literal("Color: #" + String.format("%06X", data(DATA_COLOR) & 0xFFFFFF)));
        return lines;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < machineSlotCount) {
            if (!moveItemStackTo(stack, machineSlotCount, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveToMachineSlots(stack)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        sendFactoryRunsIfChanged();
        sendEnergyHatchDataIfChanged();
        sendFluidHatchDataIfChanged();
        sendSmartInterfaceDataIfChanged();
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        sendFactoryRuns(true);
        sendEnergyHatchData(true);
        sendFluidHatchData(true);
        sendSmartInterfaceDataIfChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.level().getBlockEntity(blockPos) instanceof BaseMachineBlockEntity) {
            return player.canInteractWithBlock(blockPos, 4.0);
        }
        return false;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_REFRESH_STRUCTURE && blockEntity instanceof MachineControllerBlockEntity controller) {
            ResourceLocation machineId = controller.getMachineId().orElse(null);
            MmceControllerButtonClickEvent event = MmceEventRegistry.postMachine(new MmceControllerButtonClickEvent(controller, machineId, id));
            if (event != null && event.isCanceled()) {
                return true;
            }
            controller.refreshStructure();
            broadcastChanges();
            return true;
        }
        if (blockEntity instanceof ParallelControllerBlockEntity parallelController) {
            if (id >= BUTTON_PARALLEL_SET_BASE) {
                parallelController.setParallelism(id - BUTTON_PARALLEL_SET_BASE);
                broadcastChanges();
                return true;
            }
            int delta = parallelButtonDelta(id);
            if (delta != 0) {
                parallelController.adjustParallelism(delta);
                broadcastChanges();
                return true;
            }
        }
        return false;
    }

    private void addMachineSlots() {
        for (int slot = 0; slot < machineSlotCount; slot++) {
            SlotPosition position = machineSlotPosition(slot);
            int x = position.x();
            int y = position.y();
            addSlot(new MachineSlot(machineContainer, slot, x, y, canInsertIntoMachineSlots()));
        }
    }

    private void addPlayerInventorySlots() {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        playerInventoryX + column * 18, playerInventoryY + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, playerInventoryX + column * 18, playerInventoryY + 58));
        }
    }

    private void addSynchronizedData() {
        for (int index = 0; index < DATA_COUNT; index++) {
            final int dataIndex = index;
            if (clientSide) {
                addDataSlot(DataSlot.shared(clientData, dataIndex));
            } else {
                addDataSlot(new DataSlot() {
                    @Override
                    public int get() {
                        return computeData(dataIndex);
                    }

                    @Override
                    public void set(int value) {
                    }
                });
            }
        }
    }

    private void addControllerLines(List<Component> lines) {
        MachineControllerBlockEntity controller = blockEntity instanceof MachineControllerBlockEntity value ? value : null;
        String machine = controller == null ? "none" : controller.getMachineId().map(ResourceLocation::toString).orElse("none");
        String recipe = controller == null ? "none" : controller.getActiveRecipeId().map(ResourceLocation::toString).orElse("none");
        String detail = controller == null ? "" : controller.getRecipeStatusDetail();
        int total = Math.max(1, data(DATA_H));
        lines.add(Component.literal("Machine: " + machine));
        lines.add(Component.literal("Recipe: " + recipe));
        if (controller != null) {
            MmceControllerGUIRenderEvent event = MmceEventRegistry.postMachine(
                    new MmceControllerGUIRenderEvent(controller, controller.getMachineId().orElse(null)));
            if (event != null) {
                for (String extraLine : event.extraInfo()) {
                    if (!extraLine.isBlank()) {
                        lines.add(Component.literal(extraLine));
                    }
                }
            }
        }
        lines.add(Component.literal("Status: " + statusFromData().displayName() + (detail.isBlank() ? "" : " | " + detail)));
        lines.add(Component.literal("Progress: " + data(DATA_C) + "/" + total + "t"));
        lines.add(Component.literal("Parallelism: " + Math.max(1, data(DATA_D))));
        lines.add(Component.literal("Components: " + data(DATA_F) + " | Modifiers: " + data(DATA_G)));
        if (controller != null && !controller.getDynamicPatternMatches().isEmpty()) {
            lines.add(Component.literal("Dynamic patterns: " + controller.getDynamicPatternMatches().size()));
            for (MmceStructureMatcher.DynamicPatternMatch match : controller.getDynamicPatternMatches()) {
                lines.add(Component.literal(" - " + match.name()
                        + ": size=" + match.size()
                        + ", facing=" + match.facing().getSerializedName()));
            }
        }
    }

    private void addFactoryControllerLines(List<Component> lines) {
        MachineControllerBlockEntity controller = blockEntity instanceof MachineControllerBlockEntity value ? value : null;
        String machine = controller == null ? "none" : controller.getMachineId().map(ResourceLocation::toString).orElse("none");
        String detail = controller == null ? "" : controller.getRecipeStatusDetail();
        lines.add(Component.literal("Machine: " + machine));
        lines.add(Component.literal("Status: " + statusFromData().displayName() + (detail.isBlank() ? "" : " | " + detail)));
        lines.add(Component.literal("Threads: " + factoryRegularActiveRuns() + "/" + factoryMaxThreads()
                + " regular, " + factoryActiveRuns() + " active, " + factoryWorkingRuns() + " running"));
        lines.add(Component.literal("Parallelism: " + Math.max(1, factoryTotalParallelism())));
        lines.add(Component.literal("Components: " + data(DATA_F) + " | Modifiers: " + data(DATA_G)));
        if (controller != null) {
            MmceControllerGUIRenderEvent event = MmceEventRegistry.postMachine(
                    new MmceControllerGUIRenderEvent(controller, controller.getMachineId().orElse(null)));
            if (event != null) {
                for (String extraLine : event.extraInfo()) {
                    if (!extraLine.isBlank()) {
                        lines.add(Component.literal(extraLine));
                    }
                }
            }
        }
    }

    private void addFluidLines(List<Component> lines) {
        FluidHatchBlockEntity hatch = blockEntity instanceof FluidHatchBlockEntity value ? value : null;
        FluidStack fluid = hatch == null ? FluidStack.EMPTY : hatch.getStoredFluid();
        String fluidName = fluid.isEmpty() ? "empty" : fluid.getHoverName().getString();
        String mode = switch (kind()) {
            case FLUID_INPUT_HATCH -> "Fluid input";
            case FLUID_OUTPUT_HATCH -> "Fluid output";
            case FLUID_PROCESSOR_HATCH -> "Fluid processor";
            default -> "Fluid";
        };
        lines.add(Component.literal("Mode: " + mode));
        lines.add(Component.literal("Fluid: " + fluidName));
        lines.add(Component.literal("Fluid stored: " + fluidStored() + "/" + fluidCapacity() + " mB"));
        lines.add(Component.literal("Chemical/Gas: " + (hasStoredChemical() ? storedChemicalName() : "empty")));
        lines.add(Component.literal("Chemical stored: " + storedChemicalAmount() + "/" + fluidCapacity()));
    }

    private void addEnergyLines(List<Component> lines) {
        boolean input = energyInput();
        lines.add(Component.translatable(input ? "gui.energyhatch.mode.input" : "gui.energyhatch.mode.output"));
        lines.add(Component.translatable("gui.energyhatch.energy",
                formatNumber(energyStored()), formatNumber(energyCapacity())));
        lines.add(Component.translatable("gui.energyhatch.transfer",
                formatNumber(energyTransferLimit())));
        lines.add(Component.translatable(input ? "gui.energyhatch.side.receive" : "gui.energyhatch.side.extract"));
    }

    private void addUpgradeBusLines(List<Component> lines) {
        lines.add(Component.literal("Upgrade slots: " + data(DATA_A)));
        if (!(blockEntity instanceof UpgradeBusBlockEntity bus)) {
            return;
        }
        List<MmceMachineUpgrade> upgrades = MmceMachineUpgradeRegistry.installedUpgrades(bus);
        if (upgrades.isEmpty()) {
            lines.add(Component.literal("Installed upgrades: none"));
            return;
        }
        lines.add(Component.literal("Installed upgrades: " + upgrades.size()));
        for (MmceMachineUpgrade upgrade : upgrades) {
            lines.add(Component.literal(upgrade.getStackSize() + "x " + upgrade.getLocalizedName()));
            for (String description : upgrade.getBusGUIDescriptions()) {
                if (!description.isBlank()) {
                    lines.add(Component.literal("  " + description));
                }
            }
        }
    }

    private boolean canInsertIntoMachineSlots() {
        if (blockEntity instanceof MachineControllerBlockEntity) {
            return true;
        }
        if (blockEntity instanceof ItemBusBlockEntity bus) {
            return bus.isInput();
        }
        return blockEntity instanceof UpgradeBusBlockEntity;
    }

    private boolean moveToMachineSlots(ItemStack stack) {
        if (machineSlotCount <= 0 || stack.isEmpty()) {
            return false;
        }
        boolean changed = false;
        for (int index = 0; index < machineSlotCount && !stack.isEmpty(); index++) {
            Slot slot = slots.get(index);
            ItemStack existing = slot.getItem();
            if (!existing.isEmpty() && slot.mayPlace(stack) && ItemStack.isSameItemSameComponents(existing, stack)) {
                int inserted = Math.min(stack.getCount(), slot.getMaxStackSize(stack) - existing.getCount());
                if (inserted > 0) {
                    stack.shrink(inserted);
                    existing.grow(inserted);
                    slot.setChanged();
                    changed = true;
                }
            }
        }
        for (int index = 0; index < machineSlotCount && !stack.isEmpty(); index++) {
            Slot slot = slots.get(index);
            if (slot.getItem().isEmpty() && slot.mayPlace(stack)) {
                int inserted = Math.min(stack.getCount(), slot.getMaxStackSize(stack));
                slot.setByPlayer(stack.split(inserted));
                slot.setChanged();
                changed = true;
            }
        }
        return changed;
    }

    private int computeData(int index) {
        if (blockEntity == null) {
            return 0;
        }
        return switch (index) {
            case DATA_KIND -> kindFor(blockEntity).ordinal();
            case DATA_COLOR -> blockEntity.getMachineColor();
            case DATA_GROUP_ID -> blockEntity.getGroupId();
            case DATA_GROUP_INPUT -> blockEntity.isGroupInput() ? 1 : 0;
            case DATA_CONFIGURED_GROUP_ID -> blockEntity.getConfiguredGroupId();
            case DATA_CAN_CONFIGURE_GROUP -> blockEntity.canConfigureGroupInput() ? 1 : 0;
            default -> computeSpecificData(index);
        };
    }

    private int computeSpecificData(int index) {
        if (blockEntity instanceof MachineControllerBlockEntity controller) {
            return switch (index) {
                case DATA_A -> controller.isStructureFormed() ? 1 : 0;
                case DATA_B -> controller.isWorking() ? 1 : 0;
                case DATA_C -> controller.getRecipeProgress();
                case DATA_D -> controller.getActiveRecipeParallelism();
                case DATA_E -> controller.getRecipeStatus().ordinal();
                case DATA_F -> controller.getComponentPositions().size();
                case DATA_G -> controller.getActiveModifiers().size();
                case DATA_H -> controller.getActiveRecipeId()
                        .map(MmceDataRegistry.snapshot().recipes()::get)
                        .map(MmceRecipeDefinition::recipeTime)
                        .orElse(0);
                default -> 0;
            };
        }
        if (blockEntity instanceof EnergyHatchBlockEntity hatch) {
            return switch (index) {
                case DATA_A -> hatch.isInput() ? 1 : 0;
                case DATA_B -> low(hatch.getEnergy());
                case DATA_C -> high(hatch.getEnergy());
                case DATA_D -> low(hatch.getCapacity());
                case DATA_E -> high(hatch.getCapacity());
                case DATA_F -> low(hatch.getTransferLimit());
                case DATA_G -> high(hatch.getTransferLimit());
                default -> 0;
            };
        }
        if (blockEntity instanceof FluidHatchBlockEntity hatch) {
            return switch (index) {
                case DATA_A -> hatch.isInput() ? 1 : 0;
                case DATA_B -> hatch.isProcessor() ? 1 : 0;
                case DATA_C -> (int) Math.min(Integer.MAX_VALUE, hatch.getStoredAmount());
                case DATA_D -> hatch.getCapacity();
                case DATA_E -> hatch.getStoredChemicalAmount();
                case DATA_F -> hatch.hasStoredChemical() ? 1 : 0;
                default -> 0;
            };
        }
        if (blockEntity instanceof ItemBusBlockEntity bus) {
            return switch (index) {
                case DATA_A -> bus.isInput() ? 1 : 0;
                case DATA_B -> bus.getContainerSize();
                default -> 0;
            };
        }
        if (blockEntity instanceof UpgradeBusBlockEntity bus) {
            return index == DATA_A ? bus.getContainerSize() : 0;
        }
        if (blockEntity instanceof SmartInterfaceBlockEntity smartInterface) {
            return switch (index) {
                case DATA_A -> smartInterface.getMode().ordinal();
                case DATA_B -> smartInterface.getBindings().size();
                default -> 0;
            };
        }
        if (blockEntity instanceof ParallelControllerBlockEntity parallelController) {
            return switch (index) {
                case DATA_A -> parallelController.getParallelism();
                case DATA_B -> parallelController.getMaxParallelism();
                default -> 0;
            };
        }
        return 0;
    }

    private void sendFactoryRunsIfChanged() {
        sendFactoryRuns(false);
    }

    private void sendFactoryRuns(boolean force) {
        if (clientSide || !(blockEntity instanceof FactoryControllerBlockEntity factory)
                || !(playerInventory.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        List<FactoryControllerBlockEntity.FactoryRunView> runs = factory.factoryRunViews();
        String signature = factorySignature(factory, runs);
        if (!force && signature.equals(lastFactorySignature)) {
            return;
        }
        lastFactorySignature = signature;
        PacketDistributor.sendToPlayer(serverPlayer, new MmceFactoryRunsPayload(
                blockPos,
                containerId,
                runs,
                factory.factoryActiveRunCount(),
                factory.factoryWorkingRunCount(),
                factory.factoryRegularActiveRunCount(),
                factory.factoryMaxThreads(),
                factory.getActiveRecipeParallelism()
        ));
    }

    private void sendEnergyHatchDataIfChanged() {
        sendEnergyHatchData(false);
    }

    private void sendEnergyHatchData(boolean force) {
        if (clientSide || !(blockEntity instanceof EnergyHatchBlockEntity hatch)
                || !(playerInventory.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        String signature = hatch.isInput()
                + "|" + hatch.getEnergy()
                + "|" + hatch.getCapacity()
                + "|" + hatch.getTransferLimit();
        if (!force && signature.equals(lastEnergyHatchSignature)) {
            return;
        }
        lastEnergyHatchSignature = signature;
        PacketDistributor.sendToPlayer(serverPlayer, new MmceEnergyHatchDataPayload(
                blockPos,
                containerId,
                hatch.isInput(),
                hatch.getEnergy(),
                hatch.getCapacity(),
                hatch.getTransferLimit()
        ));
    }

    private void sendFluidHatchDataIfChanged() {
        sendFluidHatchData(false);
    }

    private void sendFluidHatchData(boolean force) {
        if (clientSide || !(blockEntity instanceof FluidHatchBlockEntity hatch)
                || !(playerInventory.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        FluidStack fluid = hatch.getStoredFluid();
        ResourceLocation fluidId = fluid.isEmpty() ? null : BuiltInRegistries.FLUID.getKey(fluid.getFluid());
        int fluidAmount = fluid.getAmount();
        ResourceLocation chemicalId = hatch.hasStoredChemical() ? hatch.getStoredChemicalId() : null;
        int chemicalAmount = hatch.getStoredChemicalAmount();
        String signature = (fluidId == null ? "empty" : fluidId.toString())
                + '|' + fluidAmount
                + '|' + (chemicalId == null ? "empty" : chemicalId.toString())
                + '|' + chemicalAmount
                + '|' + hatch.getCapacity();
        if (!force && signature.equals(lastFluidHatchSignature)) {
            return;
        }
        lastFluidHatchSignature = signature;
        PacketDistributor.sendToPlayer(serverPlayer, new MmceFluidHatchDataPayload(
                blockPos,
                containerId,
                fluidId,
                fluidAmount,
                chemicalId,
                chemicalAmount,
                hatch.getCapacity()
        ));
    }

    private void sendSmartInterfaceDataIfChanged() {
        if (clientSide || !(blockEntity instanceof SmartInterfaceBlockEntity smartInterface)
                || !(playerInventory.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        List<MmceSmartInterfaceDataPayload.BindingDetail> bindings = smartInterfaceBindingDetails(smartInterface);
        String signature = smartInterfaceSignature(bindings);
        if (signature.equals(lastSmartInterfaceSignature)) {
            return;
        }
        lastSmartInterfaceSignature = signature;
        PacketDistributor.sendToPlayer(serverPlayer, new MmceSmartInterfaceDataPayload(
                blockPos,
                containerId,
                bindings
        ));
    }

    private List<MmceSmartInterfaceDataPayload.BindingDetail> smartInterfaceBindingDetails(SmartInterfaceBlockEntity smartInterface) {
        List<SmartInterfaceBlockEntity.Binding> bindings = smartInterface.getBindings();
        List<MmceSmartInterfaceDataPayload.BindingDetail> details = new ArrayList<>(bindings.size());
        for (SmartInterfaceBlockEntity.Binding binding : bindings) {
            details.add(smartInterfaceBindingDetail(binding));
        }
        return List.copyOf(details);
    }

    private MmceSmartInterfaceDataPayload.BindingDetail smartInterfaceBindingDetail(SmartInterfaceBlockEntity.Binding binding) {
        MachineControllerBlockEntity controller = null;
        if (playerInventory.player.level().getBlockEntity(binding.controllerPos()) instanceof MachineControllerBlockEntity value) {
            controller = value;
        }
        ResourceLocation machineId = controller == null
                ? binding.machineId()
                : controller.getMachineId().orElse(binding.machineId());
        return new MmceSmartInterfaceDataPayload.BindingDetail(
                binding.controllerPos(),
                machineId,
                smartInterfaceMachineName(machineId),
                binding.type(),
                binding.value(),
                controller != null,
                controller != null && controller.isStructureFormed(),
                controller != null && controller.isWorking(),
                controller == null ? MmceRecipeStatus.IDLE : controller.getRecipeStatus(),
                controller == null ? "" : controller.getRecipeStatusDetail()
        );
    }

    private static String smartInterfaceMachineName(ResourceLocation machineId) {
        if (machineId == null) {
            return "";
        }
        var machine = MmceDataRegistry.snapshot().machines().get(machineId);
        String localizedName = machine == null ? "" : machine.localizedName();
        return localizedName.isBlank() ? prettifyIdPath(machineId) : localizedName;
    }

    private static String factorySignature(FactoryControllerBlockEntity factory, List<FactoryControllerBlockEntity.FactoryRunView> runs) {
        StringBuilder builder = new StringBuilder()
                .append(factory.factoryActiveRunCount()).append('|')
                .append(factory.factoryWorkingRunCount()).append('|')
                .append(factory.factoryRegularActiveRunCount()).append('|')
                .append(factory.factoryMaxThreads()).append('|')
                .append(factory.getActiveRecipeParallelism());
        for (FactoryControllerBlockEntity.FactoryRunView run : runs) {
            builder.append('|')
                    .append(run.coreThread()).append(',')
                    .append(run.threadName()).append(',')
                    .append(run.activeRecipeId()).append(',')
                    .append(run.progress()).append('/')
                    .append(run.totalTime()).append(',')
                    .append(run.parallelism()).append(',')
                    .append(run.working()).append(',')
                    .append(run.status().ordinal()).append(',')
                    .append(run.detail());
        }
        return builder.toString();
    }

    private static String smartInterfaceSignature(List<MmceSmartInterfaceDataPayload.BindingDetail> bindings) {
        StringBuilder builder = new StringBuilder();
        for (MmceSmartInterfaceDataPayload.BindingDetail binding : bindings) {
            builder.append('|')
                    .append(binding.controllerPos()).append(',')
                    .append(binding.machineId()).append(',')
                    .append(binding.machineName()).append(',')
                    .append(binding.type()).append(',')
                    .append(binding.value()).append(',')
                    .append(binding.controllerPresent()).append(',')
                    .append(binding.structureFormed()).append(',')
                    .append(binding.working()).append(',')
                    .append(binding.status().ordinal()).append(',')
                    .append(binding.statusDetail());
        }
        return builder.toString();
    }

    private static int parallelButtonDelta(int id) {
        return switch (id) {
            case BUTTON_PARALLEL_DECREMENT_1 -> -1;
            case BUTTON_PARALLEL_DECREMENT_10 -> -10;
            case BUTTON_PARALLEL_DECREMENT_100 -> -100;
            case BUTTON_PARALLEL_INCREMENT_1 -> 1;
            case BUTTON_PARALLEL_INCREMENT_10 -> 10;
            case BUTTON_PARALLEL_INCREMENT_100 -> 100;
            default -> 0;
        };
    }

    private int data(int index) {
        return clientSide ? clientData[index] : computeData(index);
    }

    private long unsignedLong(int lowIndex, int highIndex) {
        return (Integer.toUnsignedLong(data(highIndex)) << 32) | Integer.toUnsignedLong(data(lowIndex));
    }

    private MmceRecipeStatus statusFromData() {
        MmceRecipeStatus[] values = MmceRecipeStatus.values();
        int ordinal = data(DATA_E);
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MmceRecipeStatus.IDLE;
    }

    private static int slotCount(BaseMachineBlockEntity blockEntity) {
        return blockEntity instanceof Container container ? container.getContainerSize() : 0;
    }

    private SlotPosition machineSlotPosition(int slot) {
        return switch (fallbackKind) {
            case CONTROLLER -> new SlotPosition(151, 8);
            case FACTORY_CONTROLLER -> new SlotPosition(255, 8);
            case ITEM_INPUT_BUS, ITEM_OUTPUT_BUS -> itemBusSlotPosition(slot, machineSlotCount);
            case UPGRADE_BUS -> new SlotPosition(8 + (slot % 3) * 18, 17 + (slot / 3) * 18);
            default -> new SlotPosition(8 + (slot % 9) * 18, 18 + (slot / 9) * 18);
        };
    }

    private static SlotPosition itemBusSlotPosition(int slot, int slotCount) {
        return switch (slotCount) {
            case 1 -> new SlotPosition(81, 30);
            case 4 -> new SlotPosition(70 + (slot % 2) * 18, 18 + (slot / 2) * 18);
            case 6 -> new SlotPosition(61 + (slot % 3) * 18, 18 + (slot / 3) * 18);
            case 9 -> new SlotPosition(61 + (slot % 3) * 18, 13 + (slot / 3) * 18);
            case 12 -> new SlotPosition(52 + (slot % 4) * 18, 18 + (slot / 4) * 18);
            case 16 -> new SlotPosition(53 + (slot % 4) * 18, 8 + (slot / 4) * 18);
            case 32 -> new SlotPosition(17 + (slot % 8) * 18, 8 + (slot / 8) * 18);
            default -> {
                int columns = Math.max(1, Math.min(9, slotCount));
                int x = (DEFAULT_IMAGE_WIDTH - columns * 18) / 2 + (slot % columns) * 18;
                int y = 18 + (slot / columns) * 18;
                yield new SlotPosition(x, y);
            }
        };
    }

    private static int imageWidthFor(MachineMenuKind kind) {
        return kind == MachineMenuKind.FACTORY_CONTROLLER ? FACTORY_IMAGE_WIDTH : DEFAULT_IMAGE_WIDTH;
    }

    private static int imageHeightFor(MachineMenuKind kind) {
        return switch (kind) {
            case CONTROLLER, FACTORY_CONTROLLER, UPGRADE_BUS -> LARGE_IMAGE_HEIGHT;
            default -> DEFAULT_IMAGE_HEIGHT;
        };
    }

    private static int playerInventoryXFor(MachineMenuKind kind) {
        return kind == MachineMenuKind.FACTORY_CONTROLLER ? 112 : 8;
    }

    private static int playerInventoryYFor(MachineMenuKind kind) {
        return switch (kind) {
            case CONTROLLER, FACTORY_CONTROLLER, UPGRADE_BUS -> 131;
            default -> 84;
        };
    }

    private static int low(long value) {
        return (int) value;
    }

    private static int high(long value) {
        return (int) (value >>> 32);
    }

    private static Component titleFor(BaseMachineBlockEntity blockEntity) {
        MachineMenuKind kind = kindFor(blockEntity);
        return switch (kind) {
            case ENERGY_INPUT_HATCH -> Component.translatable("gui.energyhatch.input.title");
            case ENERGY_OUTPUT_HATCH -> Component.translatable("gui.energyhatch.output.title");
            default -> Component.literal(kind.displayName());
        };
    }

    private static String formatNumber(long value) {
        return String.format(java.util.Locale.ROOT, "%,d", value);
    }

    private static String prettifyIdPath(ResourceLocation id) {
        String path = id.getPath();
        StringBuilder builder = new StringBuilder(path.length());
        boolean capitalize = true;
        for (int index = 0; index < path.length(); index++) {
            char c = path.charAt(index);
            if (c == '_' || c == '-' || c == '/') {
                if (!builder.isEmpty() && builder.charAt(builder.length() - 1) != ' ') {
                    builder.append(' ');
                }
                capitalize = true;
                continue;
            }
            builder.append(capitalize ? Character.toUpperCase(c) : c);
            capitalize = false;
        }
        return builder.isEmpty() ? id.toString() : builder.toString();
    }

    private static MachineMenuKind kindFor(BaseMachineBlockEntity blockEntity) {
        if (blockEntity instanceof FactoryControllerBlockEntity) {
            return MachineMenuKind.FACTORY_CONTROLLER;
        }
        if (blockEntity instanceof MachineControllerBlockEntity) {
            return MachineMenuKind.CONTROLLER;
        }
        if (blockEntity instanceof ItemBusBlockEntity bus) {
            return bus.isInput() ? MachineMenuKind.ITEM_INPUT_BUS : MachineMenuKind.ITEM_OUTPUT_BUS;
        }
        if (blockEntity instanceof UpgradeBusBlockEntity) {
            return MachineMenuKind.UPGRADE_BUS;
        }
        if (blockEntity instanceof EnergyHatchBlockEntity hatch) {
            return hatch.isInput() ? MachineMenuKind.ENERGY_INPUT_HATCH : MachineMenuKind.ENERGY_OUTPUT_HATCH;
        }
        if (blockEntity instanceof FluidHatchBlockEntity hatch) {
            if (hatch.isProcessor()) {
                return MachineMenuKind.FLUID_PROCESSOR_HATCH;
            }
            return hatch.isInput() ? MachineMenuKind.FLUID_INPUT_HATCH : MachineMenuKind.FLUID_OUTPUT_HATCH;
        }
        if (blockEntity instanceof SmartInterfaceBlockEntity) {
            return MachineMenuKind.SMART_INTERFACE;
        }
        if (blockEntity instanceof ParallelControllerBlockEntity) {
            return MachineMenuKind.PARALLEL_CONTROLLER;
        }
        return MachineMenuKind.UNKNOWN;
    }

    public enum MachineMenuKind {
        UNKNOWN("Machine Component"),
        CONTROLLER("Machine Controller"),
        FACTORY_CONTROLLER("Factory Controller"),
        ITEM_INPUT_BUS("Item Input Bus"),
        ITEM_OUTPUT_BUS("Item Output Bus"),
        FLUID_INPUT_HATCH("Fluid Input Hatch"),
        FLUID_OUTPUT_HATCH("Fluid Output Hatch"),
        FLUID_PROCESSOR_HATCH("Fluid Processor Hatch"),
        ENERGY_INPUT_HATCH("Energy Input Hatch"),
        ENERGY_OUTPUT_HATCH("Energy Output Hatch"),
        SMART_INTERFACE("Smart Interface"),
        PARALLEL_CONTROLLER("Parallel Controller"),
        UPGRADE_BUS("Upgrade Bus");

        private final String displayName;

        MachineMenuKind(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

        private static MachineMenuKind byOrdinal(int ordinal) {
            MachineMenuKind[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : UNKNOWN;
        }
    }

    private static final class MachineSlot extends Slot {
        private final boolean allowPlace;
        private final int slotIndex;

        private MachineSlot(Container container, int slot, int x, int y, boolean allowPlace) {
            super(container, slot, x, y);
            this.allowPlace = allowPlace;
            this.slotIndex = slot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return allowPlace && container.canPlaceItem(slotIndex, stack);
        }
    }

    private record SlotPosition(int x, int y) {
    }
}
