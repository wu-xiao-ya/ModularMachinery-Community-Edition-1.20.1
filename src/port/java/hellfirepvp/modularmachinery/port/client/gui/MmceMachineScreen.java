package hellfirepvp.modularmachinery.port.client.gui;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import hellfirepvp.modularmachinery.port.registry.MmceMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class MmceMachineScreen extends AbstractContainerScreen<MmceMachineMenu> {
    private static final int BACKGROUND = 0xFF20252A;
    private static final int PANEL = 0xFF2C343A;
    private static final int BORDER = 0xFF6D7E86;
    private static final int SLOT = 0xFF111417;
    private static final int TEXT = 0xFFE6ECEF;
    private static final int MUTED_TEXT = 0xFFA9B6BC;
    private static final ResourceLocation GUI_CONTROLLER = texture("guicontroller_large");
    private static final ResourceLocation GUI_FACTORY = texture("guifactory");
    private static final ResourceLocation GUI_BAR = texture("guibar");
    private static final ResourceLocation GUI_EMPTY = texture("guismartinterface");
    private static final ResourceLocation GUI_UPGRADE_BUS = texture("guiupgradebus");

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
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
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
        renderTooltip(guiGraphics, mouseX, mouseY);
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
