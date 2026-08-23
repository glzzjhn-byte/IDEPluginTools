package APIDesign;

public class PluginAction {

    public enum TargetUI {
        SIDEBAR_HEADER,
        TOOLBAR_RIGHT,
        EDITOR_CONTEXT_MENU
    }

    private final TargetUI target;
    private final String label;
    private final String tooltip;
    private final String jarFilename;

    private PluginAction(Builder builder) {
        this.target = builder.target;
        this.label = builder.label;
        this.tooltip = builder.tooltip;
        this.jarFilename = builder.jarFilename;
    }

    public TargetUI getTarget() { return target; }
    public String getLabel() { return label; }
    public String getTooltip() { return tooltip; }
    public String getJarFilename() { return jarFilename; }

    public static class Builder {
        private TargetUI target;
        private String label = "Extension";
        private String tooltip = "Launch Plugin";
        private String jarFilename;

        public Builder type(TargetUI target) {
            this.target = target;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder tooltip(String tooltip) {
            this.tooltip = tooltip;
            return this;
        }


        public Builder runJar(String jarFilename) {
            if (jarFilename == null
                    || jarFilename.contains("/")
                    || jarFilename.contains("\\")
                    || jarFilename.contains("..")
                    || jarFilename.contains("\0")) {
                throw new IllegalStateException("❌ Plugin Error: jarFilename must be a bare filename, no path components.");
            }
            this.jarFilename = jarFilename;
            return this;
        }

        public PluginAction build() {
            if (this.target == null) {
                throw new IllegalStateException("\u274C Plugin Error: You must specify a .type().");
            }
            if (this.jarFilename == null || !this.jarFilename.toLowerCase().endsWith(".jar")) {
                throw new IllegalStateException("\u274C Plugin Error: You must provide a valid .jar filename using .runJar().");
            }
            return new PluginAction(this);
        }
    }
}