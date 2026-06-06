package hellfirepvp.modularmachinery.port.client.gui;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import hellfirepvp.modularmachinery.port.registry.MmceMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
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

    public MmceMachineScreen(MmceMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = menu.imageHeight();
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

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
        int y = 22;
        for (Component line : menu.statusLines()) {
            if (y >= menu.playerInventoryY() - 16) {
                break;
            }
            guiGraphics.drawString(font, line, 8, y, y == 22 ? TEXT : MUTED_TEXT, false);
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
}
