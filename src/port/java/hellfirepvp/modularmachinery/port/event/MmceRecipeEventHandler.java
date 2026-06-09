package hellfirepvp.modularmachinery.port.event;


@FunctionalInterface
public interface MmceRecipeEventHandler {
    void handle(MmceRecipeEvent event);
}
