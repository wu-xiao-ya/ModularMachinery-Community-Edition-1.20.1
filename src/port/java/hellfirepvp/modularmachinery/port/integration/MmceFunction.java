package hellfirepvp.modularmachinery.port.integration;

@FunctionalInterface
public interface MmceFunction<T, R> {
    R apply(T input);
}
