package hellfirepvp.modularmachinery.port.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

final class MmceRecipeCommands {
    static void run(MachineControllerBlockEntity controller, JsonArray commands, int tick) {
        if (commands.isEmpty() || !(controller.getLevel() instanceof ServerLevel level)) {
            return;
        }

        CommandSourceStack source = new CommandSourceStack(
                CommandSource.NULL,
                Vec3.atCenterOf(controller.getBlockPos()),
                Vec2.ZERO,
                level,
                2,
                "MMCE Recipe",
                Component.literal("MMCE Recipe"),
                level.getServer(),
                null
        ).withSuppressedOutput();

        for (JsonElement element : commands) {
            RecipeCommand command = parse(element);
            if (command == null || !command.shouldRun(tick)) {
                continue;
            }

            try {
                level.getServer().getCommands().performPrefixedCommand(source, command.command());
            } catch (RuntimeException ex) {
                ModularMachineryNeoForge.LOGGER.error("Failed to run MMCE recipe command '{}'", command.command(), ex);
            }
        }
    }

    private static RecipeCommand parse(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            String command = element.getAsString();
            return command.isBlank() ? null : new RecipeCommand(command, -1);
        }
        if (!element.isJsonObject()) {
            return null;
        }

        JsonObject object = element.getAsJsonObject();
        if (!object.has("command")) {
            return null;
        }
        String command = GsonHelper.getAsString(object, "command", "");
        if (command.isBlank()) {
            return null;
        }
        int interval = Mth.clamp(GsonHelper.getAsInt(object, "interval", -1), -1, Integer.MAX_VALUE);
        return new RecipeCommand(command, interval);
    }

    private record RecipeCommand(String command, int interval) {
        boolean shouldRun(int tick) {
            return interval <= 0 || tick % interval == 0;
        }
    }

    private MmceRecipeCommands() {
    }
}
