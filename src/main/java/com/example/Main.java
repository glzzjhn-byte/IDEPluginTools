package com.example;

import APIDesign.PluginAction;
import APIDesign.PluginBrain;

public class Main {
    public static void main(String[] args) {
        System.out.println("Bootstrapping Third-Party Plugins...");

        PluginAction gitSyncPlugin = new PluginAction.Builder()
                .type(PluginAction.TargetUI.SIDEBAR_HEADER)
                .label("Git Sync")
                .tooltip("Push and pull changes from GitHub")
                .runJar("GitSyncPlugin-1.0.jar") // Remember: No paths allowed!
                .build();

        PluginAction formatterPlugin = new PluginAction.Builder()
                .type(PluginAction.TargetUI.TOOLBAR_RIGHT)
                .label("Prettier")
                .tooltip("Format current file")
                .runJar("CodeFormatter.jar")
                .build();

        PluginBrain.registerAction(gitSyncPlugin);
        PluginBrain.registerAction(formatterPlugin);

        System.out.println("✅ Plugins registered successfully!");

        int sidebarCount = PluginBrain.getActionsFor(PluginAction.TargetUI.SIDEBAR_HEADER).size();
        int toolbarCount = PluginBrain.getActionsFor(PluginAction.TargetUI.TOOLBAR_RIGHT).size();

        System.out.println("Sidebar actions loaded: " + sidebarCount);
        System.out.println("Toolbar actions loaded: " + toolbarCount);
    }
}