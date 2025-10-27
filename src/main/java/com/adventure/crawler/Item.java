package com.adventure.crawler;

// Item represents small collectables placed in the dungeon: potions or treasure.
public class Item {
    public enum Type { POTION, TREASURE }

    private final int x;
    private final int y;
    private final Type type;

    // Construct an item at position with a given type
    public Item(int x, int y, Type type) {
        this.x = x; this.y = y; this.type = type;
    }

    // Simple accessors used when the player steps on the tile
    public int getX() { return x; }
    public int getY() { return y; }
    public Type getType() { return type; }

    // getGlyph: returns the character used to render this item on the map
    public char getGlyph() {
        return switch (type) {
            case POTION -> '!' ;
            case TREASURE -> '$' ;
        };
    }
}
