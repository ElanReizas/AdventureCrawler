package com.adventure.crawler;

// Tile enum holds the map glyph and whether it is walkable. Simple tile abstraction used by Dungeon.
public enum Tile {
    WALL('#', false),
    FLOOR('.', true);

    public final char glyph;
    public final boolean walkable;

    Tile(char glyph, boolean walkable) {
        this.glyph = glyph;
        this.walkable = walkable;
    }
}
