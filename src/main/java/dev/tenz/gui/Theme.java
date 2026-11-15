package dev.tenz.gui;

import dev.tenz.module.Modules;
import java.util.List;

/** Colors used by the menu and every overlay. Pick one in Appearance. */
public record Theme(String name, int bg, int panel, int sel, int accent, int text, int dim, int good) {
    public static final List<Theme> ALL = List.of(
        new Theme("Frost",    0xD0101014, 0xA01C1C22, 0xFF2C2C36, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF808088, 0xFF55E08A),
        new Theme("Purple",   0xD01A0F2E, 0xA0281848, 0xFF3A2466, 0xFFB26BFF, 0xFFFFFFFF, 0xFF9C88C0, 0xFF7CF0B0),
        new Theme("Ocean",    0xD0081824, 0xA0102A3E, 0xFF18405C, 0xFF4FC3F7, 0xFFFFFFFF, 0xFF7FA6BC, 0xFF55E08A),
        new Theme("Rose",     0xD02A1018, 0xA03E1A28, 0xFF5C2438, 0xFFFF6B9D, 0xFFFFFFFF, 0xFFC08898, 0xFF7CF0B0),
        new Theme("Emerald",  0xD00A1A12, 0xA0122A1C, 0xFF1C4430, 0xFF3DDC84, 0xFFFFFFFF, 0xFF80A890, 0xFFB8FF8A),
        new Theme("Sunset",   0xD02A140C, 0xA03E2014, 0xFF5C3420, 0xFFFFA24C, 0xFFFFFFFF, 0xFFC09C84, 0xFF8AF0A0),
        new Theme("Midnight", 0xE0000000, 0xA0101010, 0xFF202020, 0xFF8A8AFF, 0xFFF0F0F0, 0xFF707070, 0xFF55E08A)
    );
    public static Theme current() { return ALL.get(Math.floorMod(Modules.themeIndex, ALL.size())); }
}
