package APIDesign.DataDesign;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;

public final class MYMYDATA {

    public enum TargetData {
        RUN_COMPILING,      // builds/compiles the project — only ONE allowed (enforced in ToolBrain)
        LANGUAGE_COMPILER,  // adds support for a new language — many allowed, one per language
        STATIC_ANALYSIS,    // linters/analyzers — read-only, many allowed, run in parallel
        FORMATTER,          // reformats source on save/request — write access to current file only
        DEBUGGER,           // attaches a debugger — only ONE active at a time (like RUN_COMPILING)
        TEST_RUNNER         // runs project tests — many allowed, user picks which to invoke
    }

    public enum Capability {
        READ_SOURCE,          // read the currently-open file only
        WRITE_SOURCE,         // modify the currently-open file only (e.g. formatters)
        READ_PROJECT_FILES,   // read any file inside the project root — NOT the whole disk
        WRITE_PROJECT_FILES,  // write any file inside the project root
        RUN_PROCESS,          // spawn external processes (compilers, CLI tools)
        NETWORK,              // make network calls — highest scrutiny, off by default
        CLIPBOARD,            // read/write system clipboard
        UI_NOTIFY             // show a toast/notification to the user — harmless, safe to auto-grant
    }

    public enum TrustLevel {
        SANDBOXED,  // default for any unsigned/unknown jar — capability requests get auto-denied
        //   for anything above READ_SOURCE / UI_NOTIFY, no matter what .needs() asked for
        VERIFIED,   // dev has been through some vetting (signed jar, known publisher) — requested
        //   capabilities are granted as-is
        SYSTEM      // built-in tools shipped with the IDE itself — full access, not available to 3rd parties
    }

    public enum ExecutionTimeout {
        FAST(5),         // formatters, linters — should return almost instantly
        NORMAL(30),      // most compilers
        LONG_RUNNING(300); // full project builds, test suites

        public final int seconds;
        ExecutionTimeout(int seconds) { this.seconds = seconds; }
    }

    public enum PluginState {
        ENABLED,   // normal, eligible to run
        DISABLED,  // user turned it off — stays registered but skipped everywhere
        ERRORED    // crashed, timed out, or failed validation — IDE stops invoking it
        //   until the user explicitly re-enables it (prevents crash-loop spam)
    }

    private final String id;
    private final String version;
    private final TargetData target;
    private final String mainJarFilename;
    private final String language;
    private final Set<Capability> requestedCapabilities;
    private final TrustLevel trust;
    private final ExecutionTimeout timeout;
    private PluginState state = PluginState.ENABLED;

    private MYMYDATA(Builder b) {
        this.id = b.id;
        this.version = b.version;
        this.target = b.target;
        this.mainJarFilename = b.mainJarFilename;
        this.language = b.language;
        this.requestedCapabilities = EnumSet.copyOf(b.capabilities);
        this.trust = b.trust;
        this.timeout = b.timeout;
    }

    public String getId()                  { return id; }
    public String getVersion()             { return version; }
    public TargetData getTarget()          { return target; }
    public String getMainJarFilename()     { return mainJarFilename; }
    public String getLanguage()            { return language; }
    public TrustLevel getTrust()           { return trust; }
    public ExecutionTimeout getTimeout()   { return timeout; }
    public PluginState getState()          { return state; }
    public void setState(PluginState s)    { this.state = s; }

    // Grants only what trust level allows, regardless of what was requested.
    private Set<Capability> resolveGrantedCapabilities() {
        if (trust == TrustLevel.SANDBOXED) {
            Set<Capability> safe = EnumSet.of(Capability.READ_SOURCE, Capability.UI_NOTIFY);
            safe.retainAll(requestedCapabilities);
            return safe;
        }
        return requestedCapabilities; // VERIFIED / SYSTEM get what they asked for
    }

    // ONE definition only — checks both lifecycle state and granted capability.
    public boolean has(Capability c) {
        return state == PluginState.ENABLED && resolveGrantedCapabilities().contains(c);
    }

    /** Gated by capability — declared READ_SOURCE or you get null, not a crash. */
    public File SOURCE() {
        if (!has(Capability.READ_SOURCE)) return null;
        return PluginContext.getCurrentSourceFile();
    }

    public static class Builder {
        private String id;
        private String version = "1.0";
        private TargetData target;
        private String mainJarFilename;
        private String language;
        private boolean languageSet = false;
        private final Set<Capability> capabilities = EnumSet.noneOf(Capability.class);
        private TrustLevel trust = TrustLevel.SANDBOXED;       // safest default — must opt UP into more trust
        private ExecutionTimeout timeout = ExecutionTimeout.NORMAL; // safest default — bounded by default

        public Builder id(String id)                   { this.id = id; return this; }
        public Builder version(String version)         { this.version = version; return this; }
        public Builder type(TargetData target)         { this.target = target; return this; }
        public Builder needs(Capability c)             { this.capabilities.add(c); return this; }
        public Builder trust(TrustLevel trust)         { this.trust = trust; return this; }
        public Builder timeout(ExecutionTimeout t)     { this.timeout = t; return this; }

        public Builder ONRUN(String jarFilename) {
            if (jarFilename == null || jarFilename.contains("/") || jarFilename.contains("\\")
                    || jarFilename.contains("..") || jarFilename.contains("\0")
                    || !jarFilename.toLowerCase().endsWith(".jar")) {
                throw new IllegalStateException("❌ Tool Error: ONRUN must be a bare .jar filename.");
            }
            this.mainJarFilename = jarFilename;
            return this;
        }

        public Builder LANG_ADD(String languageName) {
            if (languageSet) throw new IllegalStateException("❌ Tool Error: LANG_ADD can only be called once.");
            if (languageName == null || languageName.isBlank())
                throw new IllegalStateException("❌ Tool Error: language name must not be blank.");
            this.language = languageName;
            this.languageSet = true;
            return this;
        }

        public MYMYDATA build() {
            if (id == null || id.isBlank())
                throw new IllegalStateException("❌ Tool Error: You must call .id() with a unique identifier.");
            if (target == null)
                throw new IllegalStateException("❌ Tool Error: You must call .type().");
            if (mainJarFilename == null)
                throw new IllegalStateException("❌ Tool Error: You must call .ONRUN() with a jar.");
            if (target == TargetData.LANGUAGE_COMPILER && language == null)
                throw new IllegalStateException("❌ Tool Error: LANGUAGE_COMPILER tools must call .LANG_ADD().");
            return new MYMYDATA(this);
        }
    }
}