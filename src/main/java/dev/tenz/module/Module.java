package dev.tenz.module;

public class Module {
    public final String name, description, category;
    public boolean enabled;
    public Module(String name, String description, String category, boolean enabled) {
        this.name = name; this.description = description; this.category = category; this.enabled = enabled;
    }
}
