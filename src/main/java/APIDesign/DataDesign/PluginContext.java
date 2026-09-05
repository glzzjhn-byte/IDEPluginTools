package APIDesign.DataDesign;

import java.io.File;
import java.util.function.Supplier;

public final class PluginContext {
    private static Supplier<File> currentSourceFileSupplier = () -> null;

    private PluginContext() {}

    public static void setCurrentSourceFileSupplier(Supplier<File> supplier) {
        currentSourceFileSupplier = supplier;
    }

    public static File getCurrentSourceFile() {
        return currentSourceFileSupplier.get();
    }
}