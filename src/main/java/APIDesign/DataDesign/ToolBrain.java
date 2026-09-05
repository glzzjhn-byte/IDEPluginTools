package APIDesign.DataDesign;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ToolBrain {

    private static final Map<MYMYDATA.TargetData, List<MYMYDATA>> registry =
            new EnumMap<>(MYMYDATA.TargetData.class);

    static {
        for (MYMYDATA.TargetData target : MYMYDATA.TargetData.values()) {
            registry.put(target, new ArrayList<>());
        }
    }

    private ToolBrain() {}

    public static void registerTool(MYMYDATA data) {
        if (registry.get(data.getTarget()).stream().anyMatch(t -> t.getId().equals(data.getId())))
            throw new IllegalStateException("❌ Tool Error: a tool with id '" + data.getId() + "' is already registered.");
        if (data.getTarget() == MYMYDATA.TargetData.RUN_COMPILING
                && !registry.get(MYMYDATA.TargetData.RUN_COMPILING).isEmpty())
            throw new IllegalStateException("❌ Tool Error: a RUN_COMPILING tool is already registered.");
        registry.get(data.getTarget()).add(data);
    }

    public static void unregisterTool(String id) {
        registry.values().forEach(list -> list.removeIf(t -> t.getId().equals(id)));
    }
}