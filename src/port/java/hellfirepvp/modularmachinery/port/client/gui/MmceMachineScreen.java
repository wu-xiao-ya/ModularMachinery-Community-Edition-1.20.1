package hellfirepvp.modularmachinery.port.client.gui;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.blockentity.FactoryControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.FluidHatchBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.SmartInterfaceBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import hellfirepvp.modularmachinery.port.network.MmceSmartInterfaceUpdatePayload;
import hellfirepvp.modularmachinery.port.registry.MmceMenus;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class MmceMachineScreen extends AbstractContainerScreen<MmceMachineMenu> {
    private static final int BACKGROUND = 0xFF20252A;
    private static final int PANEL = 0xFF2C343A;
    private static final int BORDER = 0xFF6D7E86;
    private static final int SLOT = 0xFF111417;
    private static final int TEXT = 0xFFE6ECEF;
    private static final int MUTED_TEXT = 0xFFA9B6BC;
    private static final ResourceLocation GUI_CONTROLLER = texture("guicontroller_large");
    private static final ResourceLocation GUI_FACTORY = texture("guifactory");
    private static final ResourceLocation GUI_FACTORY_ELEMENTS = texture("guifactoryelements");
    private static final ResourceLocation GUI_BAR = texture("guibar");
    private static final ResourceLocation GUI_EMPTY = texture("guismartinterface");
    private static final ResourceLocation GUI_UPGRADE_BUS = texture("guiupgradebus");
    private static final int BAR_X = 15;
    private static final int BAR_Y = 10;
    private static final int BAR_WIDTH = 20;
    private static final int BAR_HEIGHT = 61;
    private EditBox parallelismBox;
    private EditBox smartInterfaceBox;
    private Button smartPrevButton;
    private Button smartNextButton;
    private int smartInterfaceIndex;
    private int factoryScroll;

    public MmceMachineScreen(MmceMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = menu.imageWidth();
        imageHeight = menu.imageHeight();
        inventoryLabelX = menu.playerInventoryX();
        inventoryLabelY = menu.playerInventoryY() - 11;
    }

    @Override
    protected void init() {
        super.init();
        if (menu.canRefreshStructure()) {
            addRenderableWidget(Button.builder(Component.literal("Refresh"), button -> {
                if (Minecraft.getInstance().gameMode != null) {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, MmceMachineMenu.BUTTON_REFRESH_STRUCTURE);
                }
            }).bounds(leftPos + imageWidth - 66, topPos + 20, 58, 18).build());
        }
        if (menu.kind() == MmceMachineMenu.MachineMenuKind.PARALLEL_CONTROLLER) {
            addParallelControllerWidgets();
        }
        if (menu.kind() == MmceMachineMenu.MachineMenuKind.SMART_INTERFACE) {
            addSmartInterfaceWidgets();
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        TextureSpec texture = background();
        if (texture == null) {
            renderFallbackBackground(guiGraphics, left, top);
            return;
        }
        guiGraphics.blit(texture.location(), left, top, 0.0F, 0.0F, imageWidth, imageHeight,
                texture.width(), texture.height());
        if (menu.kind() == MmceMachineMenu.MachineMenuKind.UPGRADE_BUS) {
            for (int index = 0; index < menu.machineSlotCount(); index++) {
                Slot slot = menu.slots.get(index);
                guiGraphics.blit(GUI_UPGRADE_BUS, left + slot.x - 1, top + slot.y - 1,
                        7.0F, 130.0F, 18, 18, 256, 256);
            }
        }
        renderMachineOverlays(guiGraphics, left, top);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
        if (menu.kind() == MmceMachineMenu.MachineMenuKind.PARALLEL_CONTROLLER) {
            guiGraphics.drawString(font, Component.literal("Max Parallelism: " + menu.maxParallelism()), 6, 20, TEXT, true);
            guiGraphics.drawString(font, Component.literal("Current Parallelism: " + menu.parallelism()), 6, 53, TEXT, true);
            guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MUTED_TEXT, false);
            return;
        }
        if (menu.kind() == MmceMachineMenu.MachineMenuKind.SMART_INTERFACE) {
            renderSmartInterfaceLabels(guiGraphics);
            guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MUTED_TEXT, false);
            return;
        }
        if (!showStatusLines()) {
            guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MUTED_TEXT, false);
            return;
        }
        int x = statusX();
        int y = statusY();
        for (Component line : menu.statusLines()) {
            if (y >= menu.playerInventoryY() - 16) {
                break;
            }
            guiGraphics.drawString(font, line, x, y, y == statusY() ? TEXT : MUTED_TEXT, true);
            y += 10;
        }
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MUTED_TEXT, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!renderBarTooltip(guiGraphics, mouseX, mouseY)) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (parallelismBox != null && parallelismBox.isFocused() && (keyCode == 257 || keyCode == 335)) {
            submitParallelismBox();
            return true;
        }
        if (smartInterfaceBox != null && smartInterfaceBox.isFocused() && (keyCode == 257 || keyCode == 335)) {
            submitSmartInterfaceBox();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.kind() == MmceMachineMenu.MachineMenuKind.FACTORY_CONTROLLER
                && isHovering(8, 8, 96, 197, mouseX, mouseY)) {
            factoryScroll -= (int) Math.signum(scrollY);
            clampFactoryScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void containerTick() {
        if (menu.kind() == MmceMachineMenu.MachineMenuKind.SMART_INTERFACE) {
            clampSmartInterfaceIndex();
            if (smartPrevButton != null) {
                smartPrevButton.active = smartInterfaceIndex > 0;
            }
            if (smartNextButton != null) {
                smartNextButton.active = smartInterfaceIndex + 1 < menu.smartInterfaceBindings().size();
            }
            updateSmartInterfaceSuggestion();
        }
        clampFactoryScroll();
    }

    @EventBusSubscriber(modid = ModularMachineryNeoForge.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(MmceMenus.MACHINE.get(), MmceMachineScreen::new);
        }
    }

    private void renderFallbackBackground(GuiGraphics guiGraphics, int left, int top) {
        guiGraphics.fill(left, top, left + imageWidth, top + imageHeight, BACKGROUND);
        guiGraphics.fill(left, top, left + imageWidth, top + 18, PANEL);
        guiGraphics.fill(left, top, left + imageWidth, top + 1, BORDER);
        guiGraphics.fill(left, top + imageHeight - 1, left + imageWidth, top + imageHeight, BORDER);
        guiGraphics.fill(left, top, left + 1, top + imageHeight, BORDER);
        guiGraphics.fill(left + imageWidth - 1, top, left + imageWidth, top + imageHeight, BORDER);
        for (Slot slot : menu.slots) {
            guiGraphics.fill(left + slot.x - 1, top + slot.y - 1, left + slot.x + 17, top + slot.y + 17, BORDER);
            guiGraphics.fill(left + slot.x, top + slot.y, left + slot.x + 16, top + slot.y + 16, SLOT);
        }
    }

    private void renderMachineOverlays(GuiGraphics guiGraphics, int left, int top) {
        switch (menu.kind()) {
            case CONTROLLER -> renderControllerProgress(guiGraphics, left, top);
            case FACTORY_CONTROLLER -> renderFactoryQueue(guiGraphics, left, top);
            case ENERGY_INPUT_HATCH, ENERGY_OUTPUT_HATCH -> renderEnergyBar(guiGraphics, left, top);
            case FLUID_INPUT_HATCH, FLUID_OUTPUT_HATCH, FLUID_PROCESSOR_HATCH -> renderFluidBar(guiGraphics, left, top);
            default -> {
            }
        }
    }

    private void renderControllerProgress(GuiGraphics guiGraphics, int left, int top) {
        int progress = progressPixels(140);
        if (progress <= 0) {
            return;
        }
        int color = menu.working() ? 0xFF5BE37D : 0xFF8EA7B4;
        guiGraphics.fill(left + 11, top + 197, left + 11 + progress, top + 201, color);
    }

    private void renderFactoryQueue(GuiGraphics guiGraphics, int left, int top) {
        List<FactoryControllerBlockEntity.FactoryRunView> runs = menu.factoryRuns();
        clampFactoryScroll();
        int rowY = top + 8;
        for (int row = 0; row < 6; row++) {
            int y = rowY + row * 33;
            guiGraphics.blit(GUI_FACTORY_ELEMENTS, left + 8, y, 0.0F, 0.0F, 86, 32, 256, 256);
            int runIndex = factoryScroll + row;
            if (runIndex >= runs.size()) {
                continue;
            }
            renderFactoryRun(guiGraphics, runs.get(runIndex), left + 8, y, runIndex);
        }
        if (runs.size() > 6) {
            renderFactoryScrollbar(guiGraphics, left, top, runs.size());
        }
    }

    private void renderFactoryRun(GuiGraphics guiGraphics, FactoryControllerBlockEntity.FactoryRunView run, int x, int y, int index) {
        int accent = run.coreThread() ? 0xAA5AA8FF : run.working() ? 0xAA4FDB7A : 0xAA8D3D3D;
        guiGraphics.fill(x + 1, y + 1, x + 4, y + 31, accent);
        if (run.activeRecipeId() != null && run.totalTime() > 0) {
            int progress = Math.max(1, Math.min(82, (int) ((long) run.progress() * 82L / Math.max(1, run.totalTime()))));
            int progressColor = run.working() ? 0x773EE070 : 0x667B8790;
            guiGraphics.fill(x + 4, y + 26, x + 4 + progress, y + 30, progressColor);
        }

        String thread = run.coreThread()
                ? (run.threadName().isBlank() ? "Core" : run.threadName())
                : "Thread " + (index + 1);
        String status = run.status().displayName();
        int statusColor = run.working() ? 0xFF66E08F : run.activeRecipeId() == null ? 0xFFFF6D6D : 0xFFFFC857;
        drawTrimmed(guiGraphics, thread, x + 7, y + 4, 50, run.coreThread() ? 0xFF9FCBFF : TEXT);
        drawTrimmed(guiGraphics, status, x + 58, y + 4, 27, statusColor);

        String recipe = run.activeRecipeId() == null ? "No recipe" : run.activeRecipeId().getPath();
        drawTrimmed(guiGraphics, recipe, x + 7, y + 14, 80, MUTED_TEXT);
        String progressText = run.activeRecipeId() == null
                ? "Idle"
                : run.progress() + "/" + Math.max(1, run.totalTime()) + "t x" + run.parallelism();
        drawTrimmed(guiGraphics, progressText, x + 7, y + 23, 80, TEXT);
    }

    private void renderFactoryScrollbar(GuiGraphics guiGraphics, int left, int top, int runCount) {
        int trackX = left + 96;
        int trackY = top + 8;
        int trackHeight = 197;
        guiGraphics.fill(trackX, trackY, trackX + 4, trackY + trackHeight, 0x66384449);
        int maxScroll = Math.max(1, runCount - 6);
        int thumbHeight = Math.max(18, trackHeight * 6 / runCount);
        int thumbY = trackY + (trackHeight - thumbHeight) * factoryScroll / maxScroll;
        guiGraphics.fill(trackX, thumbY, trackX + 4, thumbY + thumbHeight, 0xFF8EA7B4);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int left, int top) {
        int filled = fillPixels(menu.energyStored(), menu.energyCapacity(), BAR_HEIGHT);
        if (filled > 0) {
            guiGraphics.blit(GUI_BAR, left + BAR_X, top + BAR_Y + BAR_HEIGHT - filled,
                    196.0F, BAR_HEIGHT - filled, BAR_WIDTH, filled, 256, 256);
        }
    }

    private void renderFluidBar(GuiGraphics guiGraphics, int left, int top) {
        int filled = fillPixels(menu.fluidStored(), menu.fluidCapacity(), BAR_HEIGHT);
        if (filled > 0 && !renderFluidContent(guiGraphics, left, top, filled)) {
            guiGraphics.fill(left + BAR_X + 1, top + BAR_Y + BAR_HEIGHT - filled,
                    left + BAR_X + BAR_WIDTH - 1, top + BAR_Y + BAR_HEIGHT, 0xCC4D8DFF);
        }
        guiGraphics.blit(GUI_BAR, left + BAR_X, top + BAR_Y, 176.0F, 0.0F, BAR_WIDTH, BAR_HEIGHT, 256, 256);
    }

    private boolean renderFluidContent(GuiGraphics guiGraphics, int left, int top, int filled) {
        FluidStack fluid = storedFluid();
        if (fluid.isEmpty()) {
            return false;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation stillTexture = extensions.getStillTexture(fluid);
        if (stillTexture == null) {
            return false;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int color = extensions.getTintColor(fluid);
        float alpha = ((color >>> 24) & 0xFF) / 255.0F;
        float red = ((color >>> 16) & 0xFF) / 255.0F;
        float green = ((color >>> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        guiGraphics.setColor(red, green, blue, alpha);
        int rendered = 0;
        while (rendered < filled) {
            int tileHeight = Math.min(16, filled - rendered);
            int y = top + BAR_Y + BAR_HEIGHT - filled + rendered;
            guiGraphics.blit(left + BAR_X, y, 0, BAR_WIDTH, tileHeight, sprite);
            rendered += tileHeight;
        }
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        return true;
    }

    private boolean renderBarTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isBarMenu() || !isHovering(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, mouseX, mouseY)) {
            return false;
        }
        List<Component> tooltip = switch (menu.kind()) {
            case ENERGY_INPUT_HATCH, ENERGY_OUTPUT_HATCH -> energyTooltip();
            case FLUID_INPUT_HATCH, FLUID_OUTPUT_HATCH, FLUID_PROCESSOR_HATCH -> fluidTooltip();
            default -> List.of();
        };
        if (!tooltip.isEmpty()) {
            guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            return true;
        }
        return false;
    }

    private boolean isBarMenu() {
        return switch (menu.kind()) {
            case ENERGY_INPUT_HATCH, ENERGY_OUTPUT_HATCH,
                    FLUID_INPUT_HATCH, FLUID_OUTPUT_HATCH, FLUID_PROCESSOR_HATCH -> true;
            default -> false;
        };
    }

    private List<Component> energyTooltip() {
        return List.of(Component.translatable("tooltip.energyhatch.charge",
                formatNumber(menu.energyStored()), formatNumber(menu.energyCapacity()), "FE"));
    }

    private List<Component> fluidTooltip() {
        List<Component> tooltip = new ArrayList<>();
        FluidStack fluid = storedFluid();
        if (fluid.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.fluidhatch.empty"));
            tooltip.add(Component.translatable("tooltip.fluidhatch.tank", formatNumber(menu.fluidStored()), formatNumber(menu.fluidCapacity())));
            return tooltip;
        }
        tooltip.add(Component.translatable("tooltip.fluidhatch.fluid"));
        tooltip.add(fluid.getHoverName());
        tooltip.add(Component.translatable("tooltip.fluidhatch.tank", formatNumber(fluid.getAmount()), formatNumber(menu.fluidCapacity())));
        return tooltip;
    }

    private FluidStack storedFluid() {
        BlockEntity blockEntity = Minecraft.getInstance().level == null
                ? null
                : Minecraft.getInstance().level.getBlockEntity(menu.blockPos());
        if (blockEntity instanceof FluidHatchBlockEntity hatch) {
            return hatch.getStoredFluid();
        }
        return FluidStack.EMPTY;
    }

    private int progressPixels(int width) {
        int total = menu.recipeTotalTime();
        if (total <= 0 || menu.recipeProgress() <= 0) {
            return 0;
        }
        return Math.max(1, Math.min(width, (int) ((long) menu.recipeProgress() * width / total)));
    }

    private static int fillPixels(long stored, long capacity, int height) {
        if (stored <= 0 || capacity <= 0) {
            return 0;
        }
        return Math.max(1, Math.min(height, (int) ((stored * height + capacity - 1) / capacity)));
    }

    private void addParallelControllerWidgets() {
        addParallelButton("-1", MmceMachineMenu.BUTTON_PARALLEL_DECREMENT_1, leftPos + 7, topPos + 26);
        addParallelButton("-10", MmceMachineMenu.BUTTON_PARALLEL_DECREMENT_10, leftPos + 72, topPos + 26);
        addParallelButton("-100", MmceMachineMenu.BUTTON_PARALLEL_DECREMENT_100, leftPos + 139, topPos + 26);
        addParallelButton("+1", MmceMachineMenu.BUTTON_PARALLEL_INCREMENT_1, leftPos + 7, topPos + 60);
        addParallelButton("+10", MmceMachineMenu.BUTTON_PARALLEL_INCREMENT_10, leftPos + 72, topPos + 60);
        addParallelButton("+100", MmceMachineMenu.BUTTON_PARALLEL_INCREMENT_100, leftPos + 139, topPos + 60);

        parallelismBox = new EditBox(font, leftPos + 73, topPos + 42, 94, 18, Component.literal("Parallelism"));
        parallelismBox.setMaxLength(10);
        parallelismBox.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        parallelismBox.setSuggestion(Integer.toString(menu.parallelism()));
        addRenderableWidget(parallelismBox);
    }

    private void addParallelButton(String label, int id, int x, int y) {
        addRenderableWidget(Button.builder(Component.literal(label), button -> sendMenuButton(id))
                .bounds(x, y, 30, 20)
                .build());
    }

    private void addSmartInterfaceWidgets() {
        smartInterfaceBox = new EditBox(font, leftPos + 98, topPos + 35, 70, 18, Component.literal("Value"));
        smartInterfaceBox.setMaxLength(16);
        smartInterfaceBox.setFilter(this::isSmartInterfaceInputAllowed);
        addRenderableWidget(smartInterfaceBox);

        smartPrevButton = Button.builder(Component.translatable("gui.smartinterface.prev"), button -> {
                    smartInterfaceIndex = Math.max(0, smartInterfaceIndex - 1);
                    resetSmartInterfaceInput();
                })
                .bounds(leftPos + 7, topPos + 58, 40, 20)
                .build();
        smartNextButton = Button.builder(Component.translatable("gui.smartinterface.next"), button -> {
                    smartInterfaceIndex = Math.min(Math.max(0, menu.smartInterfaceBindings().size() - 1), smartInterfaceIndex + 1);
                    resetSmartInterfaceInput();
                })
                .bounds(leftPos + 129, topPos + 58, 40, 20)
                .build();
        addRenderableWidget(smartPrevButton);
        addRenderableWidget(smartNextButton);
        resetSmartInterfaceInput();
    }

    private void submitParallelismBox() {
        String value = parallelismBox.getValue();
        if (!value.isBlank()) {
            try {
                int parallelism = Integer.parseInt(value);
                sendMenuButton(MmceMachineMenu.BUTTON_PARALLEL_SET_BASE + parallelism);
            } catch (NumberFormatException ignored) {
            }
        }
        parallelismBox.setValue("");
        parallelismBox.setSuggestion(Integer.toString(menu.parallelism()));
    }

    private void sendMenuButton(int id) {
        if (Minecraft.getInstance().gameMode != null) {
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void renderSmartInterfaceLabels(GuiGraphics guiGraphics) {
        int count = menu.smartInterfaceBindings().size();
        int current = count <= 0 ? 0 : smartInterfaceIndex + 1;
        drawTrimmed(guiGraphics, Component.translatable("gui.smartinterface.title", count, current).getString(), 4, 4, 168, TEXT);
        SmartInterfaceBlockEntity.Binding binding = currentSmartBinding();
        if (binding == null) {
            drawTrimmed(guiGraphics, Component.translatable("gui.smartinterface.notfound").getString(), 7, 18, 162, TEXT);
            return;
        }

        MmceMachineDefinition machine = binding.machineId() == null ? null : MmceDataRegistry.snapshot().machines().get(binding.machineId());
        String machineName = machine == null || machine.localizedName().isBlank()
                ? String.valueOf(binding.machineId())
                : machine.localizedName();
        drawTrimmed(guiGraphics, machineName + " (" + posText(binding.controllerPos()) + ")", 7, 18, 162, TEXT);
        drawTrimmed(guiGraphics, smartHeader(machine, binding), 7, 30, 86, MUTED_TEXT);
        drawTrimmed(guiGraphics, smartValue(machine, binding), 7, 42, 86, TEXT);
        drawTrimmed(guiGraphics, smartFooter(machine, binding), 7, 80, 162, MUTED_TEXT);
    }

    private void drawTrimmed(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth, int color) {
        if (text == null || text.isBlank()) {
            return;
        }
        guiGraphics.drawString(font, font.plainSubstrByWidth(text, maxWidth), x, y, color, true);
    }

    private String smartHeader(MmceMachineDefinition machine, SmartInterfaceBlockEntity.Binding binding) {
        MmceMachineDefinition.SmartInterfaceTypeDefinition type = smartType(machine, binding.type());
        if (type != null && !type.headerInfo().isBlank()) {
            return Component.translatable(type.headerInfo()).getString();
        }
        return "Type: " + binding.type();
    }

    private String smartValue(MmceMachineDefinition machine, SmartInterfaceBlockEntity.Binding binding) {
        MmceMachineDefinition.SmartInterfaceTypeDefinition type = smartType(machine, binding.type());
        if (type != null && !type.valueInfo().isBlank()) {
            try {
                return String.format(type.valueInfo(), binding.value());
            } catch (IllegalFormatException ignored) {
            }
        }
        return Component.translatable("gui.smartinterface.value", binding.value()).getString();
    }

    private String smartFooter(MmceMachineDefinition machine, SmartInterfaceBlockEntity.Binding binding) {
        MmceMachineDefinition.SmartInterfaceTypeDefinition type = smartType(machine, binding.type());
        if (type != null && !type.footerInfo().isBlank()) {
            return Component.translatable(type.footerInfo()).getString();
        }
        return "";
    }

    private MmceMachineDefinition.SmartInterfaceTypeDefinition smartType(MmceMachineDefinition machine, String type) {
        if (machine == null) {
            return null;
        }
        for (MmceMachineDefinition.SmartInterfaceTypeDefinition definition : machine.smartInterfaceTypes()) {
            if (definition.type().equals(type)) {
                return definition;
            }
        }
        return null;
    }

    private SmartInterfaceBlockEntity.Binding currentSmartBinding() {
        clampSmartInterfaceIndex();
        return menu.smartInterfaceBinding(smartInterfaceIndex);
    }

    private void clampSmartInterfaceIndex() {
        int count = menu.smartInterfaceBindings().size();
        if (count <= 0) {
            smartInterfaceIndex = 0;
        } else if (smartInterfaceIndex >= count) {
            smartInterfaceIndex = count - 1;
        }
    }

    private void clampFactoryScroll() {
        if (menu.kind() != MmceMachineMenu.MachineMenuKind.FACTORY_CONTROLLER) {
            factoryScroll = 0;
            return;
        }
        int maxScroll = Math.max(0, menu.factoryRuns().size() - 6);
        if (factoryScroll < 0) {
            factoryScroll = 0;
        } else if (factoryScroll > maxScroll) {
            factoryScroll = maxScroll;
        }
    }

    private void submitSmartInterfaceBox() {
        SmartInterfaceBlockEntity.Binding binding = currentSmartBinding();
        if (binding != null && !smartInterfaceBox.getValue().isBlank()) {
            try {
                float value = Float.parseFloat(smartInterfaceBox.getValue());
                if (Float.isFinite(value)) {
                    PacketDistributor.sendToServer(new MmceSmartInterfaceUpdatePayload(menu.blockPos(), binding.controllerPos(), value));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        resetSmartInterfaceInput();
    }

    private void resetSmartInterfaceInput() {
        if (smartInterfaceBox != null) {
            smartInterfaceBox.setValue("");
            updateSmartInterfaceSuggestion();
        }
    }

    private void updateSmartInterfaceSuggestion() {
        if (smartInterfaceBox != null && smartInterfaceBox.getValue().isEmpty()) {
            SmartInterfaceBlockEntity.Binding binding = currentSmartBinding();
            smartInterfaceBox.setSuggestion(binding == null ? "" : Float.toString(binding.value()));
        }
    }

    private boolean isSmartInterfaceInputAllowed(String value) {
        if (value == null || value.length() > 16) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            char c = value.charAt(index);
            if (!Character.isDigit(c) && c != '.' && c != '-' && c != '+' && c != 'e' && c != 'E') {
                return false;
            }
        }
        return true;
    }

    private static String posText(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    private static String formatNumber(long value) {
        return String.format(java.util.Locale.ROOT, "%,d", value);
    }

    private TextureSpec background() {
        return switch (menu.kind()) {
            case CONTROLLER -> new TextureSpec(GUI_CONTROLLER, 256, 256);
            case FACTORY_CONTROLLER -> new TextureSpec(GUI_FACTORY, 280, 213);
            case ITEM_INPUT_BUS, ITEM_OUTPUT_BUS -> new TextureSpec(texture("inventory_" + inventorySizeName()), 256, 256);
            case FLUID_INPUT_HATCH, FLUID_OUTPUT_HATCH, FLUID_PROCESSOR_HATCH,
                    ENERGY_INPUT_HATCH, ENERGY_OUTPUT_HATCH -> new TextureSpec(GUI_BAR, 256, 256);
            case SMART_INTERFACE, PARALLEL_CONTROLLER -> new TextureSpec(GUI_EMPTY, 256, 256);
            case UPGRADE_BUS -> new TextureSpec(GUI_UPGRADE_BUS, 256, 256);
            default -> null;
        };
    }

    private String inventorySizeName() {
        return switch (menu.machineSlotCount()) {
            case 1 -> "tiny";
            case 4 -> "small";
            case 6 -> "normal";
            case 9 -> "reinforced";
            case 12 -> "big";
            case 16 -> "huge";
            case 32 -> "ludicrous";
            default -> "normal";
        };
    }

    private boolean showStatusLines() {
        return switch (menu.kind()) {
            case ITEM_INPUT_BUS, ITEM_OUTPUT_BUS -> false;
            default -> true;
        };
    }

    private int statusX() {
        return switch (menu.kind()) {
            case UPGRADE_BUS -> 92;
            case FLUID_INPUT_HATCH, FLUID_OUTPUT_HATCH, FLUID_PROCESSOR_HATCH,
                    ENERGY_INPUT_HATCH, ENERGY_OUTPUT_HATCH -> 42;
            default -> 8;
        };
    }

    private int statusY() {
        return switch (menu.kind()) {
            case CONTROLLER, FACTORY_CONTROLLER -> 20;
            case UPGRADE_BUS -> 23;
            case FLUID_INPUT_HATCH, FLUID_OUTPUT_HATCH, FLUID_PROCESSOR_HATCH,
                    ENERGY_INPUT_HATCH, ENERGY_OUTPUT_HATCH -> 14;
            default -> 22;
        };
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, "textures/gui/" + name + ".png");
    }

    private record TextureSpec(ResourceLocation location, int width, int height) {
    }
}
