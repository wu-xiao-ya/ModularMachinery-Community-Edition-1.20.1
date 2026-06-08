package hellfirepvp.modularmachinery.port.integration.mekanism;

import hellfirepvp.modularmachinery.port.blockentity.FluidHatchBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

final class MmceMekanismChemicalHandler implements InvocationHandler {
    private final FluidHatchBlockEntity hatch;
    private final MekanismReflection reflection;

    private MmceMekanismChemicalHandler(FluidHatchBlockEntity hatch, MekanismReflection reflection) {
        this.hatch = hatch;
        this.reflection = reflection;
    }

    static void bootstrap() {
        MekanismReflection.get();
    }

    static Object create(FluidHatchBlockEntity hatch) {
        MekanismReflection reflection = MekanismReflection.get();
        return Proxy.newProxyInstance(
                reflection.handlerClass.getClassLoader(),
                new Class<?>[]{reflection.handlerClass},
                new MmceMekanismChemicalHandler(hatch, reflection));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args == null) {
            args = new Object[0];
        }
        return switch (method.getName()) {
            case "getChemicalTanks" -> 1;
            case "getChemicalInTank" -> getChemicalInTank((Integer) args[0]);
            case "setChemicalInTank" -> {
                setChemicalInTank((Integer) args[0], args[1]);
                yield null;
            }
            case "getChemicalTankCapacity" -> (long) tankCapacity((Integer) args[0]);
            case "isValid" -> isValid((Integer) args[0], args[1]);
            case "insertChemical" -> insertChemical(args);
            case "extractChemical" -> extractChemical(args);
            case "toString" -> "MMCE Mekanism chemical bridge[" + hatch.getBlockPos() + "]";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> invokeDefault(proxy, method, args);
        };
    }

    private Object getChemicalInTank(int tank) throws ReflectiveOperationException {
        if (tank != 0 || !hatch.hasStoredChemical()) {
            return reflection.emptyStack();
        }
        Object chemical = reflection.chemicalById(hatch.getStoredChemicalId());
        if (chemical == null || reflection.isEmptyChemical(chemical)) {
            return reflection.emptyStack();
        }
        return reflection.newChemicalStack(chemical, hatch.getStoredChemicalAmount());
    }

    private void setChemicalInTank(int tank, Object stack) throws ReflectiveOperationException {
        if (tank != 0 || reflection.isEmptyStack(stack)) {
            if (tank == 0) {
                hatch.clearStoredChemical();
            }
            return;
        }
        ResourceLocation chemicalId = reflection.chemicalId(stack);
        if (chemicalId == null) {
            hatch.clearStoredChemical();
            return;
        }
        hatch.setStoredChemical(chemicalId, clampAmount(reflection.stackAmount(stack)), new CompoundTag());
    }

    private long tankCapacity(int tank) {
        return tank == 0 ? hatch.getCapacity() : 0L;
    }

    private boolean isValid(int tank, Object stack) throws ReflectiveOperationException {
        return tank == 0 && canFill() && !reflection.isEmptyStack(stack);
    }

    private Object insertChemical(Object[] args) throws ReflectiveOperationException {
        int tank = args.length == 3 ? (Integer) args[0] : 0;
        Object stack = args.length == 3 ? args[1] : args[0];
        Object action = args.length == 3 ? args[2] : args[1];
        if (tank != 0 || !canFill() || reflection.isEmptyStack(stack)) {
            return reflection.copyStack(stack);
        }

        ResourceLocation chemicalId = reflection.chemicalId(stack);
        if (chemicalId == null) {
            return reflection.copyStack(stack);
        }
        if (hatch.hasStoredChemical() && !Objects.equals(hatch.getStoredChemicalId(), chemicalId)) {
            return reflection.copyStack(stack);
        }

        int amount = clampAmount(reflection.stackAmount(stack));
        int stored = hatch.getStoredChemicalAmount();
        int inserted = Math.min(amount, hatch.getCapacity() - stored);
        if (inserted <= 0) {
            return reflection.copyStack(stack);
        }

        if (reflection.executes(action)) {
            hatch.setStoredChemical(chemicalId, stored + inserted,
                    hatch.hasStoredChemical() ? hatch.getStoredChemicalNbt() : new CompoundTag());
        }
        return inserted >= amount ? reflection.emptyStack() : reflection.copyStackWithAmount(stack, amount - inserted);
    }

    private Object extractChemical(Object[] args) throws ReflectiveOperationException {
        if (args.length == 2) {
            return args[0] instanceof Number amount
                    ? extractChemical(0, amount.longValue(), args[1])
                    : extractChemical(args[0], args[1]);
        }
        if (args.length == 3 && args[0] instanceof Integer tank) {
            return extractChemical(tank, ((Number) args[1]).longValue(), args[2]);
        }
        return reflection.emptyStack();
    }

    private Object extractChemical(int tank, long amount, Object action) throws ReflectiveOperationException {
        if (tank != 0 || !canDrain() || amount <= 0 || !hatch.hasStoredChemical()) {
            return reflection.emptyStack();
        }

        Object stored = getChemicalInTank(0);
        if (reflection.isEmptyStack(stored)) {
            return reflection.emptyStack();
        }

        int extracted = Math.min(clampAmount(amount), hatch.getStoredChemicalAmount());
        if (extracted <= 0) {
            return reflection.emptyStack();
        }

        if (reflection.executes(action)) {
            int remaining = hatch.getStoredChemicalAmount() - extracted;
            hatch.setStoredChemical(hatch.getStoredChemicalId(), remaining, hatch.getStoredChemicalNbt());
        }
        return reflection.copyStackWithAmount(stored, extracted);
    }

    private Object extractChemical(Object stack, Object action) throws ReflectiveOperationException {
        if (!canDrain() || reflection.isEmptyStack(stack) || !hatch.hasStoredChemical()) {
            return reflection.emptyStack();
        }

        ResourceLocation requestedId = reflection.chemicalId(stack);
        if (!Objects.equals(hatch.getStoredChemicalId(), requestedId)) {
            return reflection.emptyStack();
        }

        return extractChemical(0, reflection.stackAmount(stack), action);
    }

    private boolean canFill() {
        return hatch.isInput() || hatch.isProcessor();
    }

    private boolean canDrain() {
        return !hatch.isInput() || hatch.isProcessor();
    }

    private int clampAmount(long amount) {
        return (int) Math.max(0L, Math.min(amount, Integer.MAX_VALUE));
    }

    private static Object invokeDefault(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return InvocationHandler.invokeDefault(proxy, method, args == null ? new Object[0] : args);
        } catch (IllegalArgumentException exception) {
            throw new UnsupportedOperationException("Unsupported Mekanism chemical handler method: " + method, exception);
        }
    }

    private static final class MekanismReflection {
        private static MekanismReflection instance;

        private final Class<?> handlerClass;
        private final Class<?> chemicalStackClass;
        private final Class<?> chemicalClass;
        private final Constructor<?> chemicalStackConstructor;
        private final Object emptyChemical;
        private final Object chemicalRegistry;
        private final Method registryGetMethod;
        private final Method registryGetKeyMethod;
        private final Method stackGetChemicalMethod;
        private final Method stackGetAmountMethod;
        private final Method stackCopyMethod;
        private final Method stackCopyWithAmountMethod;
        private final Method stackIsEmptyMethod;
        private final Method chemicalIsEmptyTypeMethod;
        private final Method actionExecuteMethod;

        static MekanismReflection get() {
            if (instance == null) {
                instance = new MekanismReflection();
            }
            return instance;
        }

        private MekanismReflection() {
            try {
                handlerClass = Class.forName("mekanism.api.chemical.IChemicalHandler");
                chemicalStackClass = Class.forName("mekanism.api.chemical.ChemicalStack");
                chemicalClass = Class.forName("mekanism.api.chemical.Chemical");
                Class<?> actionClass = Class.forName("mekanism.api.Action");
                Class<?> mekanismApiClass = Class.forName("mekanism.api.MekanismAPI");

                chemicalStackConstructor = chemicalStackClass.getConstructor(chemicalClass, long.class);
                emptyChemical = mekanismApiClass.getField("EMPTY_CHEMICAL").get(null);
                chemicalRegistry = mekanismApiClass.getField("CHEMICAL_REGISTRY").get(null);
                registryGetMethod = registryGetMethod(chemicalRegistry.getClass());
                registryGetKeyMethod = chemicalRegistry.getClass().getMethod("getKey", Object.class);
                stackGetChemicalMethod = chemicalStackClass.getMethod("getChemical");
                stackGetAmountMethod = chemicalStackClass.getMethod("getAmount");
                stackCopyMethod = chemicalStackClass.getMethod("copy");
                stackCopyWithAmountMethod = chemicalStackClass.getMethod("copyWithAmount", long.class);
                stackIsEmptyMethod = chemicalStackClass.getMethod("isEmpty");
                chemicalIsEmptyTypeMethod = chemicalClass.getMethod("isEmptyType");
                actionExecuteMethod = actionClass.getMethod("execute");
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not initialize Mekanism chemical reflection bridge", exception);
            }
        }

        private static Method registryGetMethod(Class<?> registryClass) throws NoSuchMethodException {
            try {
                return registryClass.getMethod("get", ResourceLocation.class);
            } catch (NoSuchMethodException exception) {
                return registryClass.getMethod("getValue", ResourceLocation.class);
            }
        }

        Object emptyStack() throws ReflectiveOperationException {
            return chemicalStackClass.getField("EMPTY").get(null);
        }

        Object newChemicalStack(Object chemical, long amount) throws ReflectiveOperationException {
            return chemicalStackConstructor.newInstance(chemical, amount);
        }

        Object chemicalById(ResourceLocation id) throws ReflectiveOperationException {
            return registryGetMethod.invoke(chemicalRegistry, id);
        }

        ResourceLocation chemicalId(Object stack) throws ReflectiveOperationException {
            Object chemical = stackGetChemicalMethod.invoke(stack);
            Object id = registryGetKeyMethod.invoke(chemicalRegistry, chemical);
            return id instanceof ResourceLocation resourceLocation ? resourceLocation : null;
        }

        long stackAmount(Object stack) throws ReflectiveOperationException {
            return (Long) stackGetAmountMethod.invoke(stack);
        }

        Object copyStack(Object stack) throws ReflectiveOperationException {
            return stack == null ? emptyStack() : stackCopyMethod.invoke(stack);
        }

        Object copyStackWithAmount(Object stack, long amount) throws ReflectiveOperationException {
            return stackCopyWithAmountMethod.invoke(stack, amount);
        }

        boolean isEmptyStack(Object stack) throws ReflectiveOperationException {
            return stack == null || (Boolean) stackIsEmptyMethod.invoke(stack);
        }

        boolean isEmptyChemical(Object chemical) throws ReflectiveOperationException {
            return chemical == null || chemical == emptyChemical || (Boolean) chemicalIsEmptyTypeMethod.invoke(chemical);
        }

        boolean executes(Object action) throws ReflectiveOperationException {
            return action != null && (Boolean) actionExecuteMethod.invoke(action);
        }
    }
}
