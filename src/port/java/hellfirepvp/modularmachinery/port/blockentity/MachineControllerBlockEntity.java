package hellfirepvp.modularmachinery.port.blockentity;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.block.ControllerBlock;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceIoType;
import hellfirepvp.modularmachinery.port.data.MmceMachineModifierDefinition;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition.SmartInterfaceTypeDefinition;
import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.event.MmceMachineStructureFormedEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineStructureUpdateEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineTickEvent;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgrade;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.machine.MmceStructureMatcher;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeExecutor;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineController")
public class MachineControllerBlockEntity extends BaseMachineBlockEntity {
    private boolean structureFormed;
    private boolean working;
    private ResourceLocation machineId;
    private UUID owner;
    private int structureCheckTicker;
    private List<BlockPos> componentPositions = List.of();
    private Map<BlockPos, String> componentTags = Map.of();
    private List<MmceMachineModifierDefinition> activeModifiers = List.of();
    private List<MmceStructureMatcher.DynamicPatternMatch> dynamicPatternMatches = List.of();
    private final Map<String, MmceMachineModifierDefinition> temporaryModifiers = new LinkedHashMap<>();
    private final Map<String, MmceMachineModifierDefinition> permanentModifiers = new LinkedHashMap<>();
    private boolean needsStructureRefresh;
    private ResourceLocation activeRecipeId;
    private int recipeProgress;
    private int activeRecipeParallelism = 1;
    private MmceRecipeStatus recipeStatus = MmceRecipeStatus.IDLE;
    private String recipeStatusDetail = "";

    public MachineControllerBlockEntity(BlockPos pos, BlockState state) {
        this(MmceBlockEntities.MACHINE_CONTROLLER.get(), pos, state);
    }

    protected MachineControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean isStructureFormed() {
        return structureFormed;
    }

    public void setStructureFormed(boolean structureFormed) {
        if (this.structureFormed != structureFormed) {
            this.structureFormed = structureFormed;
            markForSync();
        }
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        if (this.working != working) {
            this.working = working;
            markForSync();
        }
    }

    public Optional<ResourceLocation> getMachineId() {
        return Optional.ofNullable(machineId);
    }

    public void setMachineId(ResourceLocation machineId) {
        if (!java.util.Objects.equals(this.machineId, machineId)) {
            this.machineId = machineId;
            markForSync();
        }
    }

    public Optional<UUID> getOwner() {
        return Optional.ofNullable(owner);
    }

    public void setOwner(UUID owner) {
        if (!java.util.Objects.equals(this.owner, owner)) {
            this.owner = owner;
            markForSync();
        }
    }

    public List<BlockPos> getComponentPositions() {
        return componentPositions;
    }

    public Map<BlockPos, String> getComponentTags() {
        return componentTags;
    }

    public List<MmceMachineModifierDefinition> getActiveModifiers() {
        return activeModifiers;
    }

    public List<MmceStructureMatcher.DynamicPatternMatch> getDynamicPatternMatches() {
        return dynamicPatternMatches;
    }

    @ZenCodeType.Method
    public String[] getDynamicPatternNames() {
        return dynamicPatternMatches.stream()
                .map(MmceStructureMatcher.DynamicPatternMatch::name)
                .toArray(String[]::new);
    }

    @ZenCodeType.Method
    public int getDynamicPatternSize(String patternName) {
        return findDynamicPattern(patternName).map(MmceStructureMatcher.DynamicPatternMatch::size).orElse(0);
    }

    @ZenCodeType.Method
    public String getDynamicPatternFacing(String patternName) {
        return findDynamicPattern(patternName)
                .map(match -> match.facing().getSerializedName())
                .orElse("");
    }

    public List<MmceMachineModifierDefinition> getRecipeModifiers() {
        if (temporaryModifiers.isEmpty() && permanentModifiers.isEmpty()) {
            return activeModifiers;
        }
        List<MmceMachineModifierDefinition> modifiers = new ArrayList<>(
                activeModifiers.size() + permanentModifiers.size() + temporaryModifiers.size());
        modifiers.addAll(activeModifiers);
        modifiers.addAll(permanentModifiers.values());
        modifiers.addAll(temporaryModifiers.values());
        return List.copyOf(modifiers);
    }

    @ZenCodeType.Method
    public void addModifier(String key, MmceRecipeModifier modifier) {
        temporaryModifiers.put(normalizeModifierKey(key), runtimeModifier(modifier));
        markForSync();
    }

    @ZenCodeType.Method
    public void removeModifier(String key) {
        if (temporaryModifiers.remove(normalizeModifierKey(key)) != null) {
            markForSync();
        }
    }

    @ZenCodeType.Method
    public boolean hasModifier(String key) {
        String normalizedKey = normalizeModifierKey(key);
        return temporaryModifiers.containsKey(normalizedKey) || permanentModifiers.containsKey(normalizedKey);
    }

    @ZenCodeType.Method
    public boolean hasTemporaryModifier(String key) {
        return temporaryModifiers.containsKey(normalizeModifierKey(key));
    }

    @ZenCodeType.Method
    public void addPermanentModifier(String key, MmceRecipeModifier modifier) {
        permanentModifiers.put(normalizeModifierKey(key), runtimeModifier(modifier));
        markForSync();
    }

    @ZenCodeType.Method
    public void removePermanentModifier(String key) {
        if (permanentModifiers.remove(normalizeModifierKey(key)) != null) {
            markForSync();
        }
    }

    @ZenCodeType.Method
    public boolean hasPermanentModifier(String key) {
        return permanentModifiers.containsKey(normalizeModifierKey(key));
    }

    @ZenCodeType.Method
    public boolean hasMachineUpgrade(String upgradeName) {
        return MmceMachineUpgradeRegistry.hasInstalledUpgrade(this, upgradeName);
    }

    @ZenCodeType.Method
    public MmceMachineUpgrade getMachineUpgrade(String upgradeName) {
        return MmceMachineUpgradeRegistry.installedUpgrade(this, upgradeName).orElse(null);
    }

    @ZenCodeType.Method
    public MmceMachineUpgrade[] getFoundUpgrades() {
        return MmceMachineUpgradeRegistry.installedUpgrades(this).toArray(MmceMachineUpgrade[]::new);
    }

    public void clearTemporaryModifiers() {
        if (!temporaryModifiers.isEmpty()) {
            temporaryModifiers.clear();
            markForSync();
        }
    }

    public Optional<SmartInterfaceBlockEntity.Binding> getSmartInterfaceData(String type) {
        if (level == null || type == null || type.isBlank()) {
            return Optional.empty();
        }
        for (BlockPos pos : componentPositions) {
            if (level.getBlockEntity(pos) instanceof SmartInterfaceBlockEntity smartInterface) {
                Optional<SmartInterfaceBlockEntity.Binding> binding = smartInterface.getBinding(worldPosition)
                        .filter(data -> data.type().equals(type));
                if (binding.isPresent()) {
                    return binding;
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateSmartInterfaceValue(String type, float value) {
        if (level == null || type == null || type.isBlank()) {
            return false;
        }
        for (BlockPos pos : componentPositions) {
            if (level.getBlockEntity(pos) instanceof SmartInterfaceBlockEntity smartInterface
                    && smartInterface.getBinding(worldPosition).filter(data -> data.type().equals(type)).isPresent()) {
                return smartInterface.updateValue(worldPosition, value);
            }
        }
        return false;
    }

    public Optional<ResourceLocation> getActiveRecipeId() {
        return Optional.ofNullable(activeRecipeId);
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public int getActiveRecipeParallelism() {
        return Math.max(1, activeRecipeParallelism);
    }

    public void setActiveRecipeParallelism(int parallelism) {
        int normalized = Math.max(1, parallelism);
        if (activeRecipeParallelism != normalized) {
            activeRecipeParallelism = normalized;
            markForSync();
        }
    }

    public MmceRecipeStatus getRecipeStatus() {
        return recipeStatus;
    }

    public String getRecipeStatusDetail() {
        return recipeStatusDetail;
    }

    public void setRecipeStatus(MmceRecipeStatus status) {
        setRecipeStatus(status, "");
    }

    public void setRecipeStatus(MmceRecipeStatus status, String detail) {
        MmceRecipeStatus normalizedStatus = status == null ? MmceRecipeStatus.IDLE : status;
        String normalizedDetail = detail == null ? "" : detail;
        if (recipeStatus != normalizedStatus || !recipeStatusDetail.equals(normalizedDetail)) {
            recipeStatus = normalizedStatus;
            recipeStatusDetail = normalizedDetail;
            markForSync();
        }
    }

    public void startRecipe(ResourceLocation recipeId) {
        startRecipe(recipeId, 1);
    }

    public void startRecipe(ResourceLocation recipeId, int parallelism) {
        activeRecipeId = recipeId;
        recipeProgress = 0;
        activeRecipeParallelism = Math.max(1, parallelism);
        setWorking(true);
        setRecipeStatus(MmceRecipeStatus.RUNNING, recipeId.toString());
        markForSync();
    }

    public int advanceRecipeProgress() {
        recipeProgress++;
        setChanged();
        return recipeProgress;
    }

    public void setRecipeProgress(int progress) {
        int normalized = Math.max(0, progress);
        if (recipeProgress != normalized) {
            recipeProgress = normalized;
            markForSync();
        }
    }

    public void clearActiveRecipe() {
        if (activeRecipeId != null || recipeProgress != 0 || activeRecipeParallelism != 1) {
            activeRecipeId = null;
            recipeProgress = 0;
            activeRecipeParallelism = 1;
            markForSync();
        }
        clearTemporaryModifiers();
    }

    public void serverTick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        int delay = structureFormed ? 100 : 40;
        structureCheckTicker++;
        if (needsStructureRefresh || structureCheckTicker >= delay) {
            needsStructureRefresh = false;
            structureCheckTicker = 0;
            refreshStructure();
        }

        if (structureFormed) {
            ResourceLocation currentMachineId = machineId;
            if (currentMachineId != null) {
                MmceMachineTickEvent preTickEvent = MmceEventRegistry.postMachine(
                        new MmceMachineTickEvent(this, currentMachineId, MmceEventPhase.START));
                if (preTickEvent.isCanceled()) {
                    return;
                }
            }
            tickFormedStructure();
            if (currentMachineId != null) {
                MmceEventRegistry.postMachine(new MmceMachineTickEvent(this, currentMachineId, MmceEventPhase.END));
            }
        } else {
            tickMissingStructure();
        }
    }

    protected void tickFormedStructure() {
        MmceRecipeExecutor.tick(this);
    }

    protected void tickMissingStructure() {
        setWorking(false);
        clearActiveRecipe();
        setRecipeStatus(MmceRecipeStatus.STRUCTURE_MISSING);
    }

    public boolean refreshStructure() {
        if (level == null || level.isClientSide()) {
            return false;
        }

        Direction facing = getBlockState().hasProperty(ControllerBlock.FACING)
                ? getBlockState().getValue(ControllerBlock.FACING)
                : Direction.NORTH;
        Optional<MmceStructureMatcher.MatchResult> matched = machineId == null
                ? MmceStructureMatcher.findFirstMatch(level, worldPosition, facing, this::canAutoMatchMachine)
                : MmceStructureMatcher.findByIdMatch(level, worldPosition, facing, machineId)
                        .filter(result -> canMatchBoundMachine(result.machine()))
                        .or(() -> MmceStructureMatcher.findFirstMatch(level, worldPosition, facing, this::canAutoMatchMachine));

        List<BlockPos> oldComponentPositions = componentPositions;
        boolean wasFormed = structureFormed;
        ResourceLocation oldMachineId = machineId;
        boolean formed = matched.isPresent();
        ResourceLocation newMachineId = matched.map(result -> result.machine().id()).orElse(machineId);
        List<BlockPos> newComponentPositions = matched.map(MmceStructureMatcher.MatchResult::componentPositions).orElse(List.of());
        Map<BlockPos, String> newComponentTags = matched.map(MmceStructureMatcher.MatchResult::componentTags).orElse(Map.of());
        List<MmceMachineModifierDefinition> newActiveModifiers = matched.map(MmceStructureMatcher.MatchResult::activeModifiers).orElse(List.of());
        List<MmceStructureMatcher.DynamicPatternMatch> newDynamicPatternMatches = matched
                .map(MmceStructureMatcher.MatchResult::dynamicPatterns)
                .orElse(List.of());
        boolean machineChanged = !java.util.Objects.equals(machineId, newMachineId);
        boolean changed = structureFormed != formed
                || machineChanged
                || !componentPositions.equals(newComponentPositions)
                || !componentTags.equals(newComponentTags)
                || !activeModifiers.equals(newActiveModifiers)
                || !dynamicPatternMatches.equals(newDynamicPatternMatches);

        structureFormed = formed;
        machineId = newMachineId;
        componentPositions = newComponentPositions;
        componentTags = newComponentTags;
        activeModifiers = newActiveModifiers;
        dynamicPatternMatches = newDynamicPatternMatches;
        if (!formed || machineChanged) {
            clearActiveRecipe();
            setWorking(false);
            setRecipeStatus(formed ? MmceRecipeStatus.IDLE : MmceRecipeStatus.STRUCTURE_MISSING);
        }
        if (formed) {
            matched.map(MmceStructureMatcher.MatchResult::machine)
                    .or(() -> MmceDataRegistry.getMachine(newMachineId))
                    .ifPresent(this::syncSmartInterfaces);
        } else {
            clearSmartInterfaceBindings(oldComponentPositions);
        }
        if (getBlockState().hasProperty(ControllerBlock.FORMED) && getBlockState().getValue(ControllerBlock.FORMED) != formed) {
            level.setBlock(worldPosition, getBlockState().setValue(ControllerBlock.FORMED, formed), 3);
        }
        if (changed) {
            markForSync();
            if (formed && newMachineId != null) {
                MmceEventRegistry.postMachine(new MmceMachineStructureUpdateEvent(this, newMachineId));
                if (!wasFormed || !java.util.Objects.equals(oldMachineId, newMachineId)) {
                    MmceEventRegistry.postMachine(new MmceMachineStructureFormedEvent(this, newMachineId));
                }
            }
        }
        return formed;
    }

    protected boolean canAutoMatchMachine(MmceMachineDefinition machine) {
        return !machine.requiresBlueprint() && canMatchBoundMachine(machine);
    }

    protected boolean canMatchBoundMachine(MmceMachineDefinition machine) {
        return !machine.factoryOnly();
    }

    private void syncSmartInterfaces(MmceMachineDefinition machine) {
        if (level == null) {
            return;
        }
        List<SmartInterfaceTypeDefinition> types = machine.smartInterfaceTypes();
        int smartIndex = 0;
        for (BlockPos pos : componentPositions) {
            if (!(level.getBlockEntity(pos) instanceof SmartInterfaceBlockEntity smartInterface)) {
                continue;
            }
            if (types.isEmpty()) {
                smartInterface.removeBinding(worldPosition);
                continue;
            }
            SmartInterfaceTypeDefinition type = types.get(Math.min(smartIndex, types.size() - 1));
            smartInterface.bind(worldPosition, machine.id(), type.type(), type.defaultValue(), false);
            smartIndex++;
        }
    }

    private void clearSmartInterfaceBindings(List<BlockPos> positions) {
        if (level == null) {
            return;
        }
        for (BlockPos pos : positions) {
            if (level.getBlockEntity(pos) instanceof SmartInterfaceBlockEntity smartInterface) {
                smartInterface.removeBinding(worldPosition);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        structureFormed = tag.getBoolean("formed");
        working = tag.getBoolean("working");
        needsStructureRefresh = structureFormed;
        activeModifiers = List.of();
        dynamicPatternMatches = List.of();
        componentTags = Map.of();
        temporaryModifiers.clear();
        permanentModifiers.clear();
        machineId = tag.contains("machineId") ? ResourceLocation.tryParse(tag.getString("machineId")) : null;
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        loadRuntimeModifiers(tag, "semiPermanentModifiers", temporaryModifiers);
        loadRuntimeModifiers(tag, "temporaryModifiers", temporaryModifiers);
        loadRuntimeModifiers(tag, "permanentModifiers", permanentModifiers);
        activeRecipeId = tag.contains("activeRecipeId") ? ResourceLocation.tryParse(tag.getString("activeRecipeId")) : null;
        recipeProgress = tag.getInt("recipeProgress");
        activeRecipeParallelism = Math.max(1, tag.getInt("activeRecipeParallelism"));
        recipeStatus = MmceRecipeStatus.bySerializedName(tag.getString("recipeStatus"));
        recipeStatusDetail = tag.getString("recipeStatusDetail");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("formed", structureFormed);
        tag.putBoolean("working", working);
        if (machineId != null) {
            tag.putString("machineId", machineId.toString());
        }
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        if (!temporaryModifiers.isEmpty()) {
            tag.put("semiPermanentModifiers", saveRuntimeModifiers(temporaryModifiers));
        }
        if (!permanentModifiers.isEmpty()) {
            tag.put("permanentModifiers", saveRuntimeModifiers(permanentModifiers));
        }
        if (activeRecipeId != null) {
            tag.putString("activeRecipeId", activeRecipeId.toString());
            tag.putInt("recipeProgress", recipeProgress);
            tag.putInt("activeRecipeParallelism", getActiveRecipeParallelism());
        }
        tag.putString("recipeStatus", recipeStatus.serializedName());
        if (!recipeStatusDetail.isBlank()) {
            tag.putString("recipeStatusDetail", recipeStatusDetail);
        }
    }

    protected static String normalizeModifierKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("MMCE recipe modifier key cannot be blank.");
        }
        return key.trim();
    }

    private Optional<MmceStructureMatcher.DynamicPatternMatch> findDynamicPattern(String patternName) {
        if (patternName == null || patternName.isBlank()) {
            return Optional.empty();
        }
        String normalized = patternName.trim();
        return dynamicPatternMatches.stream()
                .filter(match -> match.name().equals(normalized))
                .findFirst();
    }

    protected static MmceMachineModifierDefinition runtimeModifier(MmceRecipeModifier modifier) {
        if (modifier == null) {
            throw new IllegalArgumentException("MMCE recipe modifier cannot be null.");
        }
        return new MmceMachineModifierDefinition(
                List.of(0),
                List.of(0),
                List.of(0),
                List.of(),
                Optional.empty(),
                Optional.empty(),
                new JsonObject(),
                new JsonObject(),
                parseModifierTarget(modifier.getTarget()),
                MmceIoType.byName(modifier.getIOTarget()),
                modifier.getOperation(),
                modifier.modifier(),
                modifier.affectsChance(),
                modifier.json()
        );
    }

    protected static void loadRuntimeModifiers(CompoundTag tag, String listName, Map<String, MmceMachineModifierDefinition> target) {
        if (!tag.contains(listName, Tag.TAG_LIST)) {
            return;
        }
        ListTag modifiers = tag.getList(listName, Tag.TAG_COMPOUND);
        for (int index = 0; index < modifiers.size(); index++) {
            CompoundTag entry = modifiers.getCompound(index);
            String key = entry.getString("key");
            if (key.isBlank()) {
                continue;
            }
            CompoundTag modifierTag = entry.contains("modifier", Tag.TAG_COMPOUND)
                    ? entry.getCompound("modifier")
                    : entry;
            readRuntimeModifier(modifierTag).ifPresent(modifier -> target.put(key, modifier));
        }
    }

    private static Optional<MmceMachineModifierDefinition> readRuntimeModifier(CompoundTag tag) {
        if (!tag.contains("target", Tag.TAG_STRING) || tag.getString("target").isBlank()) {
            return Optional.empty();
        }

        Optional<MmceIoType> ioType = Optional.empty();
        if (tag.contains("io", Tag.TAG_STRING)) {
            ioType = MmceIoType.byName(tag.getString("io"));
        } else if (tag.contains("ioTarget", Tag.TAG_ANY_NUMERIC)) {
            ioType = Optional.of(tag.getByte("ioTarget") == 0 ? MmceIoType.INPUT : MmceIoType.OUTPUT);
        }

        double multiplier = tag.contains("multiplier", Tag.TAG_ANY_NUMERIC)
                ? tag.getDouble("multiplier")
                : tag.getFloat("value");
        boolean affectChance = tag.getBoolean("affectChance")
                || tag.getBoolean("affectsChance")
                || tag.getBoolean("chance");

        return Optional.of(new MmceMachineModifierDefinition(
                List.of(0),
                List.of(0),
                List.of(0),
                List.of(),
                Optional.empty(),
                Optional.empty(),
                new JsonObject(),
                new JsonObject(),
                parseModifierTarget(tag.getString("target")),
                ioType,
                tag.contains("operation", Tag.TAG_ANY_NUMERIC) ? tag.getInt("operation") : 0,
                multiplier,
                affectChance,
                new JsonObject()
        ));
    }

    protected static ListTag saveRuntimeModifiers(Map<String, MmceMachineModifierDefinition> modifiers) {
        ListTag list = new ListTag();
        modifiers.forEach((key, modifier) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("key", key);
            entry.put("modifier", saveRuntimeModifier(modifier));
            list.add(entry);
        });
        return list;
    }

    private static CompoundTag saveRuntimeModifier(MmceMachineModifierDefinition modifier) {
        CompoundTag tag = new CompoundTag();
        tag.putString("target", modifier.target().toString());
        modifier.ioType().ifPresent(ioType -> tag.putString("io", ioType.name().toLowerCase(java.util.Locale.ROOT)));
        tag.putInt("operation", modifier.operation());
        tag.putDouble("multiplier", modifier.multiplier());
        tag.putBoolean("affectChance", modifier.affectChance());
        return tag;
    }

    private static ResourceLocation parseModifierTarget(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("MMCE recipe modifier target cannot be blank.");
        }
        return id.indexOf(':') >= 0
                ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, id);
    }
}
