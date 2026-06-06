package hellfirepvp.modularmachinery.port.blockentity;

import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition.CoreThreadDefinition;
import hellfirepvp.modularmachinery.port.data.MmceMachineModifierDefinition;
import hellfirepvp.modularmachinery.port.data.MmceRecipeDefinition;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.recipe.MmceMachineComponents;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeExecutor;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class FactoryControllerBlockEntity extends MachineControllerBlockEntity {
    private final List<FactoryRun> factoryRuns = new ArrayList<>();
    private final Map<String, FactoryRun> coreFactoryRuns = new LinkedHashMap<>();

    public FactoryControllerBlockEntity(BlockPos pos, BlockState state) {
        super(MmceBlockEntities.FACTORY_CONTROLLER.get(), pos, state);
    }

    @Override
    protected boolean canMatchBoundMachine(MmceMachineDefinition machine) {
        return machine.hasFactory() || machine.factoryOnly();
    }

    @Override
    protected void tickFormedStructure() {
        Optional<ResourceLocation> machineId = getMachineId();
        if (level == null || level.isClientSide()) {
            return;
        }
        if (machineId.isEmpty()) {
            clearFactoryRuns();
            setWorking(false);
            setRecipeStatus(MmceRecipeStatus.NO_MACHINE);
            return;
        }

        Optional<MmceMachineDefinition> machine = MmceDataRegistry.getMachine(machineId.get());
        if (machine.isEmpty()) {
            clearFactoryRuns();
            setWorking(false);
            setRecipeStatus(MmceRecipeStatus.NO_MACHINE, "Unknown factory machine: " + machineId.get());
            return;
        }

        int maxThreads = machine.get().maxThreads();
        syncCoreFactoryRuns(machine.get());

        MmceMachineComponents components = MmceMachineComponents.collect(level, getComponentPositions(), getComponentTags());
        int machineParallelism = MmceRecipeExecutor.machineParallelism(machineId.get(), components);
        MmceRecipeExecutor.RunTickResult lastResult = new MmceRecipeExecutor.RunTickResult(
                false,
                false,
                MmceRecipeStatus.IDLE,
                maxThreads <= 0 ? "Factory threads disabled" : ""
        );

        for (FactoryRun run : coreFactoryRuns.values()) {
            lastResult = MmceRecipeExecutor.tickRun(
                    this,
                    run,
                    machineId.get(),
                    components,
                    true,
                    availableParallelism(machineParallelism, run),
                    recipe -> canStartRecipeThread(recipe) && canRunOnThread(recipe, run)
            );
        }

        if (maxThreads <= 0 && !factoryRuns.isEmpty()) {
            factoryRuns.clear();
            markForSync();
        }
        for (FactoryRun run : List.copyOf(factoryRuns)) {
            lastResult = MmceRecipeExecutor.tickRun(
                    this,
                    run,
                    machineId.get(),
                    components,
                    false,
                    availableParallelism(machineParallelism, run)
            );
        }
        removeIdleRuns();

        if (maxThreads > 0 && regularActiveRunCount() < maxThreads) {
            FactoryRun starter = new FactoryRun();
            lastResult = MmceRecipeExecutor.tickRun(
                    this,
                    starter,
                    machineId.get(),
                    components,
                    true,
                    availableParallelism(machineParallelism),
                    recipe -> canStartRecipeThread(recipe) && canRunOnThread(recipe, starter)
            );
            if (starter.getActiveRecipeId().isPresent()) {
                factoryRuns.add(starter);
                markForSync();
            }
        }

        updateFactorySummary(lastResult.status(), lastResult.detail(), maxThreads);
    }

    @Override
    protected void tickMissingStructure() {
        clearFactoryRuns();
        super.tickMissingStructure();
    }

    @Override
    public Optional<ResourceLocation> getActiveRecipeId() {
        return factoryRuns.stream()
                .map(FactoryRun::getActiveRecipeId)
                .flatMap(Optional::stream)
                .findFirst()
                .or(() -> coreFactoryRuns.values().stream()
                        .map(FactoryRun::getActiveRecipeId)
                        .flatMap(Optional::stream)
                        .findFirst())
                .or(super::getActiveRecipeId);
    }

    @Override
    public int getRecipeProgress() {
        return factoryRuns.stream()
                .filter(run -> run.getActiveRecipeId().isPresent())
                .findFirst()
                .map(FactoryRun::getRecipeProgress)
                .or(() -> coreFactoryRuns.values().stream()
                        .filter(run -> run.getActiveRecipeId().isPresent())
                        .findFirst()
                        .map(FactoryRun::getRecipeProgress))
                .orElseGet(super::getRecipeProgress);
    }

    @Override
    public int getActiveRecipeParallelism() {
        int totalParallelism = 0;
        for (FactoryRun run : factoryRuns) {
            if (run.getActiveRecipeId().isPresent()) {
                totalParallelism += run.getActiveRecipeParallelism();
            }
        }
        for (FactoryRun run : coreFactoryRuns.values()) {
            if (run.getActiveRecipeId().isPresent()) {
                totalParallelism += run.getActiveRecipeParallelism();
            }
        }
        return totalParallelism > 0 ? totalParallelism : super.getActiveRecipeParallelism();
    }

    public List<FactoryRunView> factoryRunViews() {
        List<FactoryRunView> views = new ArrayList<>(coreFactoryRuns.size() + factoryRuns.size());
        coreFactoryRuns.values().forEach(run -> views.add(factoryRunView(run)));
        factoryRuns.forEach(run -> views.add(factoryRunView(run)));
        return List.copyOf(views);
    }

    public int factoryActiveRunCount() {
        return activeRunCount();
    }

    public int factoryRegularActiveRunCount() {
        return regularActiveRunCount();
    }

    public int factoryWorkingRunCount() {
        return workingRunCount();
    }

    public int factoryMaxThreads() {
        return getMachineId()
                .flatMap(MmceDataRegistry::getMachine)
                .map(MmceMachineDefinition::maxThreads)
                .orElse(0);
    }

    @Override
    public void clearActiveRecipe() {
        super.clearActiveRecipe();
        clearFactoryRuns();
    }

    private FactoryRunView factoryRunView(FactoryRun run) {
        ResourceLocation recipeId = run.getActiveRecipeId().orElse(null);
        int totalTime = recipeId == null
                ? 0
                : Optional.ofNullable(MmceDataRegistry.snapshot().recipes().get(recipeId))
                .map(MmceRecipeDefinition::recipeTime)
                .orElse(0);
        return new FactoryRunView(
                run.isCoreThread(),
                run.threadName(),
                recipeId,
                run.getRecipeProgress(),
                totalTime,
                run.getActiveRecipeParallelism(),
                run.isWorking(),
                run.getRecipeStatus(),
                run.getRecipeStatusDetail()
        );
    }

    private void updateFactorySummary(MmceRecipeStatus fallbackStatus, String fallbackDetail, int maxThreads) {
        int active = activeRunCount();
        int working = workingRunCount();
        setWorking(working > 0);
        if (active <= 0) {
            setRecipeStatus(fallbackStatus, fallbackDetail);
            return;
        }

        Optional<FactoryRun> firstActive = factoryRuns.stream()
                .filter(run -> run.getActiveRecipeId().isPresent())
                .findFirst()
                .or(() -> coreFactoryRuns.values().stream()
                        .filter(run -> run.getActiveRecipeId().isPresent())
                        .findFirst());
        MmceRecipeStatus status = working > 0
                ? MmceRecipeStatus.RUNNING
                : firstActive.map(FactoryRun::getRecipeStatus).orElse(MmceRecipeStatus.IDLE);
        String detail = "Factory active " + active + ", regular " + regularActiveRunCount() + "/" + maxThreads + ", running " + working;
        firstActive.map(FactoryRun::getRecipeStatusDetail)
                .filter(value -> !value.isBlank())
                .ifPresent(value -> setRecipeStatus(status, detail + " | " + value));
        if (firstActive.map(FactoryRun::getRecipeStatusDetail).orElse("").isBlank()) {
            setRecipeStatus(status, detail);
        }
    }

    private int activeRunCount() {
        int active = 0;
        for (FactoryRun run : factoryRuns) {
            if (run.getActiveRecipeId().isPresent()) {
                active++;
            }
        }
        for (FactoryRun run : coreFactoryRuns.values()) {
            if (run.getActiveRecipeId().isPresent()) {
                active++;
            }
        }
        return active;
    }

    private int regularActiveRunCount() {
        int active = 0;
        for (FactoryRun run : factoryRuns) {
            if (run.getActiveRecipeId().isPresent()) {
                active++;
            }
        }
        return active;
    }

    private int workingRunCount() {
        int working = 0;
        for (FactoryRun run : factoryRuns) {
            if (run.getActiveRecipeId().isPresent() && run.isWorking()) {
                working++;
            }
        }
        for (FactoryRun run : coreFactoryRuns.values()) {
            if (run.getActiveRecipeId().isPresent() && run.isWorking()) {
                working++;
            }
        }
        return working;
    }

    private boolean canStartRecipeThread(MmceRecipeDefinition recipe) {
        int maxRecipeThreads = recipe.maxThreads();
        return maxRecipeThreads < 0 || activeRunCount(recipe.id()) < maxRecipeThreads;
    }

    private boolean canRunOnThread(MmceRecipeDefinition recipe, FactoryRun run) {
        if (!run.canSearch(recipe.id())) {
            return false;
        }
        String requiredThread = recipe.threadName();
        if (requiredThread.isBlank()) {
            return true;
        }
        return requiredThread.equals(run.threadName());
    }

    private int activeRunCount(ResourceLocation recipeId) {
        int active = 0;
        for (FactoryRun run : factoryRuns) {
            if (run.getActiveRecipeId().filter(recipeId::equals).isPresent()) {
                active++;
            }
        }
        for (FactoryRun run : coreFactoryRuns.values()) {
            if (run.getActiveRecipeId().filter(recipeId::equals).isPresent()) {
                active++;
            }
        }
        return active;
    }

    private int availableParallelism(int maxParallelism) {
        return availableParallelism(maxParallelism, null);
    }

    private int availableParallelism(int maxParallelism, FactoryRun ignoredRun) {
        int usedExtraParallelism = 0;
        for (FactoryRun run : factoryRuns) {
            if (run != ignoredRun && run.getActiveRecipeId().isPresent()) {
                usedExtraParallelism += Math.max(0, run.getActiveRecipeParallelism() - 1);
            }
        }
        for (FactoryRun run : coreFactoryRuns.values()) {
            if (run != ignoredRun && run.getActiveRecipeId().isPresent()) {
                usedExtraParallelism += Math.max(0, run.getActiveRecipeParallelism() - 1);
            }
        }
        return Math.max(1, maxParallelism - usedExtraParallelism);
    }

    private void removeIdleRuns() {
        if (factoryRuns.removeIf(run -> run.getActiveRecipeId().isEmpty())) {
            markForSync();
        }
    }

    private void clearFactoryRuns() {
        if (!factoryRuns.isEmpty() || !coreFactoryRuns.isEmpty()) {
            factoryRuns.clear();
            coreFactoryRuns.clear();
            markForSync();
        }
    }

    private void syncCoreFactoryRuns(MmceMachineDefinition machine) {
        List<CoreThreadDefinition> definitions = machine.coreThreads();
        if (definitions.isEmpty()) {
            if (!coreFactoryRuns.isEmpty()) {
                coreFactoryRuns.clear();
                markForSync();
            }
            return;
        }

        List<String> names = definitions.stream().map(CoreThreadDefinition::threadName).toList();
        if (coreFactoryRuns.keySet().removeIf(name -> !names.contains(name))) {
            markForSync();
        }
        for (CoreThreadDefinition definition : definitions) {
            FactoryRun run = coreFactoryRuns.computeIfAbsent(definition.threadName(), key -> {
                markForSync();
                return new FactoryRun(true, key);
            });
            run.setRecipeWhitelist(definition.recipes());
            run.setPresetPermanentModifiers(definition.permanentModifiers());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        factoryRuns.clear();
        if (tag.contains("factoryRuns", Tag.TAG_LIST)) {
            ListTag runs = tag.getList("factoryRuns", Tag.TAG_COMPOUND);
            for (int index = 0; index < runs.size(); index++) {
                loadFactoryRun(runs.getCompound(index)).ifPresent(factoryRuns::add);
            }
        }
        coreFactoryRuns.clear();
        if (tag.contains("coreFactoryRuns", Tag.TAG_LIST)) {
            ListTag runs = tag.getList("coreFactoryRuns", Tag.TAG_COMPOUND);
            for (int index = 0; index < runs.size(); index++) {
                loadFactoryRun(runs.getCompound(index))
                        .filter(FactoryRun::isCoreThread)
                        .ifPresent(run -> coreFactoryRuns.put(run.threadName(), run));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!factoryRuns.isEmpty()) {
            ListTag runs = new ListTag();
            for (FactoryRun run : factoryRuns) {
                runs.add(run.save());
            }
            tag.put("factoryRuns", runs);
        }
        if (!coreFactoryRuns.isEmpty()) {
            ListTag runs = new ListTag();
            for (FactoryRun run : coreFactoryRuns.values()) {
                runs.add(run.save());
            }
            tag.put("coreFactoryRuns", runs);
        }
    }

    private Optional<FactoryRun> loadFactoryRun(CompoundTag tag) {
        boolean coreThread = tag.getBoolean("coreThread") || tag.contains("coreThreadName", Tag.TAG_STRING);
        String threadName = tag.contains("threadName", Tag.TAG_STRING)
                ? tag.getString("threadName")
                : tag.getString("coreThreadName");
        FactoryRun run = coreThread ? new FactoryRun(true, threadName) : new FactoryRun();
        run.activeRecipeId = tag.contains("activeRecipeId", Tag.TAG_STRING)
                ? ResourceLocation.tryParse(tag.getString("activeRecipeId"))
                : null;
        run.recipeProgress = tag.getInt("recipeProgress");
        run.activeRecipeParallelism = Math.max(1, tag.getInt("activeRecipeParallelism"));
        run.working = tag.getBoolean("working");
        run.recipeStatus = MmceRecipeStatus.bySerializedName(tag.getString("recipeStatus"));
        run.recipeStatusDetail = tag.getString("recipeStatusDetail");
        loadRuntimeModifiers(tag, "semiPermanentModifiers", run.temporaryModifiers);
        loadRuntimeModifiers(tag, "temporaryModifiers", run.temporaryModifiers);
        loadRuntimeModifiers(tag, "permanentModifiers", run.permanentModifiers);
        return Optional.of(run);
    }

    public record FactoryRunView(
            boolean coreThread,
            String threadName,
            ResourceLocation activeRecipeId,
            int progress,
            int totalTime,
            int parallelism,
            boolean working,
            MmceRecipeStatus status,
            String detail
    ) {
        public FactoryRunView {
            threadName = threadName == null ? "" : threadName;
            progress = Math.max(0, progress);
            totalTime = Math.max(0, totalTime);
            parallelism = Math.max(1, parallelism);
            status = status == null ? MmceRecipeStatus.IDLE : status;
            detail = detail == null ? "" : detail;
        }
    }

    private final class FactoryRun implements MmceRecipeExecutor.RecipeRun {
        private final boolean coreThread;
        private final String threadName;
        private List<ResourceLocation> recipeWhitelist = List.of();
        private ResourceLocation activeRecipeId;
        private int recipeProgress;
        private int activeRecipeParallelism = 1;
        private boolean working;
        private MmceRecipeStatus recipeStatus = MmceRecipeStatus.IDLE;
        private String recipeStatusDetail = "";
        private final Map<String, MmceMachineModifierDefinition> temporaryModifiers = new LinkedHashMap<>();
        private final Map<String, MmceMachineModifierDefinition> permanentModifiers = new LinkedHashMap<>();

        private FactoryRun() {
            this(false, "");
        }

        private FactoryRun(boolean coreThread, String threadName) {
            this.coreThread = coreThread;
            this.threadName = threadName == null ? "" : threadName.trim();
        }

        @Override
        public boolean isCoreThread() {
            return coreThread;
        }

        @Override
        public String threadName() {
            return threadName;
        }

        private boolean canSearch(ResourceLocation recipeId) {
            return recipeWhitelist.isEmpty() || recipeWhitelist.contains(recipeId);
        }

        private void setRecipeWhitelist(List<ResourceLocation> recipeWhitelist) {
            this.recipeWhitelist = List.copyOf(recipeWhitelist == null ? List.of() : recipeWhitelist);
        }

        private void setPresetPermanentModifiers(Map<String, MmceMachineModifierDefinition> modifiers) {
            Map<String, MmceMachineModifierDefinition> normalized = new LinkedHashMap<>();
            if (modifiers != null) {
                modifiers.forEach((key, modifier) -> {
                    if (modifier != null) {
                        normalized.put(normalizeModifierKey(key), modifier);
                    }
                });
            }
            if (!permanentModifiers.equals(normalized)) {
                permanentModifiers.clear();
                permanentModifiers.putAll(normalized);
                markForSync();
            }
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            if (coreThread) {
                tag.putBoolean("coreThread", true);
                tag.putString("threadName", threadName);
                tag.putString("coreThreadName", threadName);
            }
            if (activeRecipeId != null) {
                tag.putString("activeRecipeId", activeRecipeId.toString());
            }
            tag.putInt("recipeProgress", recipeProgress);
            tag.putInt("activeRecipeParallelism", activeRecipeParallelism);
            tag.putBoolean("working", working);
            tag.putString("recipeStatus", recipeStatus.serializedName());
            if (!recipeStatusDetail.isBlank()) {
                tag.putString("recipeStatusDetail", recipeStatusDetail);
            }
            if (!temporaryModifiers.isEmpty()) {
                tag.put("semiPermanentModifiers", saveRuntimeModifiers(temporaryModifiers));
            }
            if (!permanentModifiers.isEmpty()) {
                tag.put("permanentModifiers", saveRuntimeModifiers(permanentModifiers));
            }
            return tag;
        }

        @Override
        public Optional<ResourceLocation> getActiveRecipeId() {
            return Optional.ofNullable(activeRecipeId);
        }

        @Override
        public int getRecipeProgress() {
            return recipeProgress;
        }

        @Override
        public int getActiveRecipeParallelism() {
            return Math.max(1, activeRecipeParallelism);
        }

        @Override
        public void setActiveRecipeParallelism(int parallelism) {
            int normalized = Math.max(1, parallelism);
            if (activeRecipeParallelism != normalized) {
                activeRecipeParallelism = normalized;
                markForSync();
            }
        }

        @Override
        public MmceRecipeStatus getRecipeStatus() {
            return recipeStatus;
        }

        @Override
        public String getRecipeStatusDetail() {
            return recipeStatusDetail;
        }

        @Override
        public boolean isWorking() {
            return working;
        }

        @Override
        public void startRecipe(ResourceLocation recipeId, int parallelism) {
            activeRecipeId = recipeId;
            recipeProgress = 0;
            activeRecipeParallelism = Math.max(1, parallelism);
            setWorking(true);
            setRecipeStatus(MmceRecipeStatus.RUNNING, recipeId.toString());
            markForSync();
        }

        @Override
        public int advanceRecipeProgress() {
            recipeProgress++;
            setChanged();
            return recipeProgress;
        }

        @Override
        public void setRecipeProgress(int progress) {
            int normalized = Math.max(0, progress);
            if (recipeProgress != normalized) {
                recipeProgress = normalized;
                markForSync();
            }
        }

        @Override
        public void rewindRecipeProgress() {
            if (recipeProgress > 0) {
                recipeProgress--;
                setChanged();
            }
        }

        @Override
        public void clearActiveRecipe() {
            if (activeRecipeId != null || recipeProgress != 0 || activeRecipeParallelism != 1) {
                activeRecipeId = null;
                recipeProgress = 0;
                activeRecipeParallelism = 1;
                markForSync();
            }
            if (!temporaryModifiers.isEmpty()) {
                temporaryModifiers.clear();
                markForSync();
            }
        }

        @Override
        public void setWorking(boolean working) {
            if (this.working != working) {
                this.working = working;
                markForSync();
            }
        }

        @Override
        public void setRecipeStatus(MmceRecipeStatus status) {
            setRecipeStatus(status, "");
        }

        @Override
        public void setRecipeStatus(MmceRecipeStatus status, String detail) {
            MmceRecipeStatus normalizedStatus = status == null ? MmceRecipeStatus.IDLE : status;
            String normalizedDetail = detail == null ? "" : detail;
            if (recipeStatus != normalizedStatus || !recipeStatusDetail.equals(normalizedDetail)) {
                recipeStatus = normalizedStatus;
                recipeStatusDetail = normalizedDetail;
                markForSync();
            }
        }

        @Override
        public boolean isFactoryRun() {
            return true;
        }

        @Override
        public List<MmceMachineModifierDefinition> getRecipeModifiers(MachineControllerBlockEntity controller) {
            List<MmceMachineModifierDefinition> controllerModifiers = controller.getRecipeModifiers();
            if (temporaryModifiers.isEmpty() && permanentModifiers.isEmpty()) {
                return controllerModifiers;
            }
            List<MmceMachineModifierDefinition> modifiers = new ArrayList<>(
                    controllerModifiers.size() + permanentModifiers.size() + temporaryModifiers.size());
            modifiers.addAll(controllerModifiers);
            modifiers.addAll(permanentModifiers.values());
            modifiers.addAll(temporaryModifiers.values());
            return List.copyOf(modifiers);
        }

        @Override
        public void addModifier(MachineControllerBlockEntity controller, String key, MmceRecipeModifier modifier) {
            temporaryModifiers.put(normalizeModifierKey(key), runtimeModifier(modifier));
            markForSync();
        }

        @Override
        public void removeModifier(MachineControllerBlockEntity controller, String key) {
            if (temporaryModifiers.remove(normalizeModifierKey(key)) != null) {
                markForSync();
            }
        }

        @Override
        public boolean hasModifier(MachineControllerBlockEntity controller, String key) {
            String normalizedKey = normalizeModifierKey(key);
            return temporaryModifiers.containsKey(normalizedKey) || permanentModifiers.containsKey(normalizedKey);
        }

        @Override
        public void addPermanentModifier(MachineControllerBlockEntity controller, String key, MmceRecipeModifier modifier) {
            permanentModifiers.put(normalizeModifierKey(key), runtimeModifier(modifier));
            markForSync();
        }

        @Override
        public void removePermanentModifier(MachineControllerBlockEntity controller, String key) {
            if (permanentModifiers.remove(normalizeModifierKey(key)) != null) {
                markForSync();
            }
        }

        @Override
        public boolean hasPermanentModifier(MachineControllerBlockEntity controller, String key) {
            return permanentModifiers.containsKey(normalizeModifierKey(key));
        }

    }
}
