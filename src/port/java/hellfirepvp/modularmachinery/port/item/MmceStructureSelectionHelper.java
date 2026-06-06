package hellfirepvp.modularmachinery.port.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.LevelResource;

public final class MmceStructureSelectionHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Map<UUID, LinkedHashSet<BlockPos>> SELECTIONS = new ConcurrentHashMap<>();

    public static ToggleResult toggle(ServerPlayer player, BlockPos pos) {
        LinkedHashSet<BlockPos> selected = SELECTIONS.computeIfAbsent(player.getUUID(), ignored -> new LinkedHashSet<>());
        BlockPos immutable = pos.immutable();
        boolean added = selected.add(immutable);
        if (!added) {
            selected.remove(immutable);
        }
        return new ToggleResult(added, selected.size());
    }

    public static int selectedCount(ServerPlayer player) {
        return SELECTIONS.getOrDefault(player.getUUID(), new LinkedHashSet<>()).size();
    }

    public static void clear(ServerPlayer player) {
        SELECTIONS.remove(player.getUUID());
    }

    public static ExportResult export(ServerPlayer player, ServerLevel level, BlockPos controllerPos, Direction controllerFacing) throws IOException {
        Set<BlockPos> selected = SELECTIONS.getOrDefault(player.getUUID(), new LinkedHashSet<>());
        if (selected.isEmpty()) {
            throw new IllegalStateException("No selected blocks.");
        }

        String fileStem = exportFileStem(player, controllerPos);
        String registryPath = "construct_tool/" + fileStem;
        JsonArray parts = new JsonArray();
        selected.stream()
                .sorted(Comparator.<BlockPos>comparingInt(BlockPos::getY)
                        .thenComparingInt(BlockPos::getX)
                        .thenComparingInt(BlockPos::getZ))
                .forEach(pos -> appendPart(level, controllerPos, controllerFacing, pos, parts));
        if (parts.isEmpty()) {
            throw new IllegalStateException("Selection did not contain exportable machine parts.");
        }

        JsonObject root = new JsonObject();
        root.addProperty("registryname", registryPath);
        root.addProperty("localizedname", "Construct Tool Machine " + fileStem);
        root.add("parts", parts);

        Path packRoot = ensurePack(level);
        Path machineDir = packRoot.resolve("data")
                .resolve(ModularMachineryNeoForge.MODID)
                .resolve("mmce_machinery")
                .resolve("construct_tool");
        Files.createDirectories(machineDir);
        Path out = machineDir.resolve(fileStem + ".json");
        Files.writeString(out, GSON.toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
        clear(player);
        return new ExportResult(
                ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, registryPath),
                out,
                parts.size());
    }

    private static void appendPart(ServerLevel level, BlockPos controllerPos, Direction controllerFacing, BlockPos selectedPos, JsonArray parts) {
        if (selectedPos.equals(controllerPos)) {
            return;
        }

        BlockState state = level.getBlockState(selectedPos);
        if (state.isAir()) {
            return;
        }

        BlockPos relative = selectedPos.subtract(controllerPos);
        BlockPos northRelative = rotateToNorth(relative, controllerFacing);
        JsonObject part = new JsonObject();
        part.addProperty("x", northRelative.getX());
        part.addProperty("y", northRelative.getY());
        part.addProperty("z", northRelative.getZ());
        part.addProperty("elements", stateDescriptor(state));
        parts.add(part);
    }

    private static Path ensurePack(ServerLevel level) throws IOException {
        Path packRoot = level.getServer().getWorldPath(LevelResource.DATAPACK_DIR).resolve("mmce_construct_tool");
        Files.createDirectories(packRoot);
        Path mcmeta = packRoot.resolve("pack.mcmeta");
        if (Files.notExists(mcmeta)) {
            JsonObject pack = new JsonObject();
            JsonObject root = new JsonObject();
            pack.addProperty("pack_format", 48);
            pack.addProperty("description", "MMCE construct tool exports");
            root.add("pack", pack);
            Files.writeString(mcmeta, GSON.toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
        }
        return packRoot;
    }

    private static String exportFileStem(ServerPlayer player, BlockPos controllerPos) {
        String name = player.getGameProfile().getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-.]", "_");
        String time = FILE_TIME.format(LocalDateTime.now());
        return name + "_" + controllerPos.getX() + "_" + controllerPos.getY() + "_" + controllerPos.getZ() + "_" + time;
    }

    private static BlockPos rotateToNorth(BlockPos pos, Direction facing) {
        Direction current = facing;
        BlockPos rotated = pos;
        while (current != Direction.NORTH) {
            current = current.getClockWise();
            rotated = new BlockPos(-rotated.getZ(), rotated.getY(), rotated.getX());
        }
        return rotated;
    }

    private static String stateDescriptor(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (state.getProperties().isEmpty()) {
            return id.toString();
        }

        StringBuilder out = new StringBuilder(id.toString()).append('[');
        boolean first = true;
        for (Property<?> property : state.getProperties().stream().sorted(Comparator.comparing(Property::getName)).toList()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            out.append(property.getName()).append('=').append(propertyValue(state, property));
        }
        return out.append(']').toString();
    }

    private static <T extends Comparable<T>> String propertyValue(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    public record ToggleResult(boolean selected, int totalSelected) {
    }

    public record ExportResult(ResourceLocation machineId, Path path, int partCount) {
    }

    private MmceStructureSelectionHelper() {
    }
}
