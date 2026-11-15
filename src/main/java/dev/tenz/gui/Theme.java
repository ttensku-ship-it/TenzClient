package dev.tenz.gui;

import dev.tenz.module.Modules;
import java.util.List;

/** Colors used by the menu and every overlay. Pick one in Appearance. */
public record Theme(String name, int bg, int panel, int sel, int accent, int text, int dim, int good) {
    public static final List<Theme> ALL = List.of(
        new Theme("Frost", 0x99101014, 0x2CFFFFFF, 0x58FFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF808088, 0xFF55E08A),
        new Theme("Purple", 0x991A0F2E, 0x2CB26BFF, 0x58B26BFF, 0xFFB26BFF, 0xFFFFFFFF, 0xFF9C88C0, 0xFF7CF0B0),
        new Theme("Ocean", 0x99081824, 0x2C4FC3F7, 0x584FC3F7, 0xFF4FC3F7, 0xFFFFFFFF, 0xFF7FA6BC, 0xFF55E08A),
        new Theme("Rose", 0x992A1018, 0x2CFF6B9D, 0x58FF6B9D, 0xFFFF6B9D, 0xFFFFFFFF, 0xFFC08898, 0xFF7CF0B0),
        new Theme("Emerald", 0x990A1A12, 0x2C3DDC84, 0x583DDC84, 0xFF3DDC84, 0xFFFFFFFF, 0xFF80A890, 0xFFB8FF8A),
        new Theme("Sunset", 0x992A140C, 0x2CFFA24C, 0x58FFA24C, 0xFFFFA24C, 0xFFFFFFFF, 0xFFC09C84, 0xFF8AF0A0),
        new Theme("Midnight", 0x99000000, 0x2C8A8AFF, 0x588A8AFF, 0xFF8A8AFF, 0xFFF0F0F0, 0xFF707070, 0xFF55E08A)
    );
    public static Theme current() { return ALL.get(Math.floorMod(Modules.themeIndex, ALL.size())); }
}
