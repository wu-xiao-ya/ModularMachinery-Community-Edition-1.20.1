package hellfirepvp.modularmachinery.port.client.gui;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.item.MmceMachineSummaryText;
import hellfirepvp.modularmachinery.port.machine.MmceStructurePreview;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class MmceBlueprintScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, "textures/gui/guiblueprint_new.png");
    private static final int IMAGE_WIDTH = 184;
    private static final int IMAGE_HEIGHT = 220;
    private static final int TEXT = 0xFFE6ECEF;
    private static final int MUTED_TEXT = 0xFF7D8790;
    private static final int GRID_BACKGROUND = 0xDD111417;
    private static final int GRID_LINE = 0xFF4A545C;
    private static final int CELL_EMPTY = 0xFF1B2228;
    private static final int CELL_FILLED = 0xFF4EC1B6;
    private static final int CELL_CONTROLLER = 0xFFFFC85A;
    private static final int CELL_DYNAMIC = 0xFF9B7CFF;
    private final MmceMachineDefinition machine;
    private final List<MmceStructurePreview.Entry> entries;
    private final List<Component> summaryLines;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;
    private int layerY;
    private Button previousLayerButton;
    private Button nextLayerButton;

    private MmceBlueprintScreen(MmceMachineDefinition machine) {
        super(Component.literal(machine.localizedName()));
        this.machine = machine;
        this.entries = new ArrayList<>(MmceStructurePreview.build(machine, Direction.NORTH));
        this.entries.sort(Comparator
                .comparingInt((MmceStructurePreview.Entry entry) -> entry.offset().getY())
                .thenComparingInt(entry -> entry.offset().getZ())
                .thenComparingInt(entry -> entry.offset().getX()));
        this.summaryLines = MmceMachineSummaryText.chatSummary(machine, false);
        Bounds bounds = Bounds.from(entries);
        this.minX = bounds.minX();
        this.maxX = bounds.maxX();
        this.minY = bounds.minY();
        this.maxY = bounds.maxY();
        this.minZ = bounds.minZ();
        this.maxZ = bounds.maxZ();
        this.layerY = 0 >= minY && 0 <= maxY ? 0 : minY;
    }

    public static void open(MmceMachineDefinition machine) {
        Minecraft.getInstance().setScreen(new MmceBlueprintScreen(machine));
    }

    @Override
    protected void init() {
        int left = left();
        int top = top();
        previousLayerButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> changeLayer(-1))
                .bounds(left + 12, top + 187, 24, 18)
                .build());
        nextLayerButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> changeLayer(1))
                .bounds(left + 148, top + 187, 24, 18)
                .build());
        updateLayerButtons();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int left = left();
        int top = top();
        guiGraphics.blit(BACKGROUND, left, top, 0.0F, 0.0F, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);
        renderHeader(guiGraphics, left, top);
        renderStructureLayer(guiGraphics, left + 16, top + 48, 152, 104);
        renderSummary(guiGraphics, left, top);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderHoveredCellTooltip(guiGraphics, left + 16, top + 48, 152, 104, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderHeader(GuiGraphics guiGraphics, int left, int top) {
        guiGraphics.drawString(font, trim(machine.localizedName(), 166), left + 10, top + 10, TEXT, false);
        guiGraphics.drawString(font, machine.id().toString(), left + 10, top + 22, MUTED_TEXT, false);
        guiGraphics.drawString(font, "Layer Y " + layerY + " / " + minY + ".." + maxY,
                left + 10, top + 36, MUTED_TEXT, false);
    }

    private void renderStructureLayer(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, GRID_LINE);
        guiGraphics.fill(x, y, x + width, y + height, GRID_BACKGROUND);
        if (entries.isEmpty()) {
            guiGraphics.drawCenteredString(font, Component.literal("No preview data"), x + width / 2, y + height / 2 - 4, MUTED_TEXT);
            return;
        }

        int spanX = Math.max(1, maxX - minX + 1);
        int spanZ = Math.max(1, maxZ - minZ + 1);
        int cell = Math.max(3, Math.min(width / spanX, height / spanZ));
        int gridWidth = cell * spanX;
        int gridHeight = cell * spanZ;
        int gridX = x + (width - gridWidth) / 2;
        int gridY = y + (height - gridHeight) / 2;

        for (int gx = 0; gx <= spanX; gx++) {
            int lineX = gridX + gx * cell;
            guiGraphics.fill(lineX, gridY, lineX + 1, gridY + gridHeight, GRID_LINE);
        }
        for (int gz = 0; gz <= spanZ; gz++) {
            int lineY = gridY + gz * cell;
            guiGraphics.fill(gridX, lineY, gridX + gridWidth, lineY + 1, GRID_LINE);
        }

        for (MmceStructurePreview.Entry entry : entries) {
            BlockPos offset = entry.offset();
            if (offset.getY() != layerY) {
                continue;
            }
            int cellX = gridX + (offset.getX() - minX) * cell + 1;
            int cellY = gridY + (offset.getZ() - minZ) * cell + 1;
            int color = cellColor(entry, offset);
            guiGraphics.fill(cellX, cellY, cellX + Math.max(1, cell - 1), cellY + Math.max(1, cell - 1), color);
        }
    }

    private void renderSummary(GuiGraphics guiGraphics, int left, int top) {
        int y = top + 157;
        int maxWidth = IMAGE_WIDTH - 20;
        for (Component line : summaryLines) {
            guiGraphics.drawString(font, trim(line.getString(), maxWidth), left + 10, y, MUTED_TEXT, false);
            y += 10;
            if (y > top + 184) {
                break;
            }
        }
    }

    private void renderHoveredCellTooltip(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        if (mouseX < x || mouseY < y || mouseX >= x + width || mouseY >= y + height || entries.isEmpty()) {
            return;
        }
        int spanX = Math.max(1, maxX - minX + 1);
        int spanZ = Math.max(1, maxZ - minZ + 1);
        int cell = Math.max(3, Math.min(width / spanX, height / spanZ));
        int gridWidth = cell * spanX;
        int gridHeight = cell * spanZ;
        int gridX = x + (width - gridWidth) / 2;
        int gridY = y + (height - gridHeight) / 2;
        int localX = mouseX - gridX;
        int localY = mouseY - gridY;
        if (localX < 0 || localY < 0 || localX >= gridWidth || localY >= gridHeight) {
            return;
        }
        int offsetX = minX + localX / cell;
        int offsetZ = minZ + localY / cell;
        entries.stream()
                .filter(entry -> {
                    BlockPos offset = entry.offset();
                    return offset.getX() == offsetX && offset.getY() == layerY && offset.getZ() == offsetZ;
                })
                .findFirst()
                .ifPresent(entry -> guiGraphics.renderTooltip(font, tooltipFor(entry, offsetX, offsetZ).stream()
                        .map(Component::getVisualOrderText)
                        .toList(), mouseX, mouseY));
    }

    private List<Component> tooltipFor(MmceStructurePreview.Entry entry, int offsetX, int offsetZ) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("Offset: " + offsetX + ", " + layerY + ", " + offsetZ));
        entry.preferredState()
                .map(BlockState::getBlock)
                .map(block -> block.getName().getString())
                .ifPresent(name -> tooltip.add(Component.literal(name)));
        return tooltip;
    }

    private int cellColor(MmceStructurePreview.Entry entry, BlockPos offset) {
        if (offset.equals(BlockPos.ZERO)) {
            return CELL_CONTROLLER;
        }
        return entry.preferredState().isPresent() ? CELL_FILLED : CELL_DYNAMIC;
    }

    private void changeLayer(int delta) {
        layerY = Math.max(minY, Math.min(maxY, layerY + delta));
        updateLayerButtons();
    }

    private void updateLayerButtons() {
        if (previousLayerButton != null) {
            previousLayerButton.active = layerY > minY;
        }
        if (nextLayerButton != null) {
            nextLayerButton.active = layerY < maxY;
        }
    }

    private int left() {
        return (width - IMAGE_WIDTH) / 2;
    }

    private int top() {
        return (height - IMAGE_HEIGHT) / 2;
    }

    private String trim(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("..."))) + "...";
    }

    private record Bounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        static Bounds from(List<MmceStructurePreview.Entry> entries) {
            if (entries.isEmpty()) {
                return new Bounds(0, 0, 0, 0, 0, 0);
            }
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (MmceStructurePreview.Entry entry : entries) {
                BlockPos offset = entry.offset();
                minX = Math.min(minX, offset.getX());
                maxX = Math.max(maxX, offset.getX());
                minY = Math.min(minY, offset.getY());
                maxY = Math.max(maxY, offset.getY());
                minZ = Math.min(minZ, offset.getZ());
                maxZ = Math.max(maxZ, offset.getZ());
            }
            return new Bounds(minX, maxX, minY, maxY, minZ, maxZ);
        }
    }
}
