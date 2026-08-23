package APIDesign;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PluginBrain {

    private static final Map<PluginAction.TargetUI, List<PluginAction>> registry = new EnumMap<>(PluginAction.TargetUI.class);
    private static Runnable uiRefreshHook;

    static {
        for (PluginAction.TargetUI target : PluginAction.TargetUI.values()) {
            registry.put(target, new ArrayList<>());
        }
    }

    public static void registerAction(PluginAction action) {
        registry.get(action.getTarget()).add(action);

        if (uiRefreshHook != null) {
            uiRefreshHook.run();
        }
    }


    public static List<PluginAction> getActionsFor(PluginAction.TargetUI target) {
        return registry.get(target);
    }

    public static void setUiRefreshHook(Runnable hook) {
        uiRefreshHook = hook;
    }
}