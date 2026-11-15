package dev.tenz.gui;

/** A clickable rectangle rebuilt every frame by the screens. */
public record Btn(int x, int y, int w, int h, Runnable action) {
    public boolean hit(double mx, double my) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }
}
