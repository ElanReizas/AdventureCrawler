package com.adventure.crawler;

import java.util.ArrayList;
import java.util.List;

// Dungeon holds the tile map, lists of enemies and items, and provides helpers
// for querying and mutating the world (walkability, occupancies, item pickup, etc.).
public class Dungeon {
    private final int width;
    private final int height;
    private final Tile[][] tiles;
    private final List<Enemy> enemies;
    private final List<Item> items;
    private int startX;
    private int startY;

    // Constructor initializes map with all walls; generator will carve floors/rooms.
    public Dungeon(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[height][width];
        this.enemies = new ArrayList<>();
        this.items = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = Tile.WALL;
            }
        }
    }

    // Basic getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // Returns tile at coordinates; out-of-bounds treated as WALL.
    public Tile getTile(int x, int y) {
        if (!isInBounds(x, y)) return Tile.WALL;
        return tiles[y][x];
    }

    // Set a tile (used by generator to carve floors/corridors).
    public void setTile(int x, int y, Tile tile) {
        if (isInBounds(x, y)) tiles[y][x] = tile;
    }

    // Bounds check helper
    public boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    // Walkability: whether tile exists and is flagged walkable
    public boolean isWalkable(int x, int y) {
        return isInBounds(x, y) && getTile(x, y).walkable;
    }

    // Access to enemy/item lists (used for updating and iteration)
    public List<Enemy> getEnemies() { return enemies; }
    public List<Item> getItems() { return items; }

    // Add enemy or item to the dungeon
    public void addEnemy(Enemy e) { enemies.add(e); }
    public void addItem(Item i) { items.add(i); }

    // Set and get starting position (generator sets this)
    public void setStart(int x, int y) {
        this.startX = x; this.startY = y;
    }

    public int getStartX() { return startX; }
    public int getStartY() { return startY; }

    // Check if a tile is occupied by a living enemy
    public boolean isOccupiedByEnemy(int x, int y) {
        for (Enemy e : enemies) {
            if (e.getX() == x && e.getY() == y && e.isAlive()) return true;
        }
        return false;
    }

    // Return living enemy at location or null
    public Enemy getEnemyAt(int x, int y) {
        for (Enemy e : enemies) {
            if (e.getX() == x && e.getY() == y && e.isAlive()) return e;
        }
        return null;
    }

    // Return item at location or null
    public Item getItemAt(int x, int y) {
        for (Item i : items) {
            if (i.getX() == x && i.getY() == y) return i;
        }
        return null;
    }

    // Remove an item (called after pickup)
    public void removeItem(Item item) { items.remove(item); }
}
