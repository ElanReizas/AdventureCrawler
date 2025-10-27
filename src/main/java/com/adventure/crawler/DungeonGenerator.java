package com.adventure.crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// DungeonGenerator: procedural generation utilities for carving rooms and corridors,
// placing enemies and items. Uses a seeded RNG to produce reproducible dungeons.
public final class DungeonGenerator {
    private DungeonGenerator() {}

    private static final int MIN_ROOM_SIZE = 4;
    private static final int MAX_ROOM_SIZE = 8;

    // generate: Builds a Dungeon of given size using the provided seed. Places a number of enemies and items
    // roughly specified by desiredEnemies/desireItems. Algorithm: place random non-overlapping rooms, connect
    // them with L-shaped corridors, set the player start as the center of the first room, then scatter enemies/items.
    public static Dungeon generate(int width, int height, long seed, int desiredEnemies, int desiredItems) {
        Random rng = new Random(seed);
        Dungeon dungeon = new Dungeon(width, height);

        List<Rect> rooms = new ArrayList<>();
        int attempts = 0;
        int maxAttempts = 200;
        // Try to create up to 10 rooms with a bounded number of attempts
        while (rooms.size() < 10 && attempts < maxAttempts) {
            attempts++;
            int w = rng.nextInt(MAX_ROOM_SIZE - MIN_ROOM_SIZE + 1) + MIN_ROOM_SIZE;
            int h = rng.nextInt(MAX_ROOM_SIZE - MIN_ROOM_SIZE + 1) + MIN_ROOM_SIZE;
            int x = rng.nextInt(Math.max(1, width - w - 1)) + 1;
            int y = rng.nextInt(Math.max(1, height - h - 1)) + 1;
            Rect room = new Rect(x, y, w, h);
            boolean overlaps = false;
            for (Rect r : rooms) {
                if (room.intersects(r)) { overlaps = true; break; }
            }
            if (!overlaps) {
                rooms.add(room);
                carveRoom(dungeon, room);
            }
        }

        // Fallback to a single large room if generation failed
        if (rooms.isEmpty()) {
            Rect center = new Rect(width/4, height/4, width/2, height/2);
            rooms.add(center);
            carveRoom(dungeon, center);
        }

        // Connect rooms to ensure a traversable dungeon
        connectRooms(dungeon, rooms, rng);

        // Start position: center of first room
        Rect startRoom = rooms.get(0);
        int startX = startRoom.centerX();
        int startY = startRoom.centerY();
        dungeon.setStart(startX, startY);

        // Place enemies randomly on walkable, unoccupied tiles, avoiding immediate start vicinity
        int placedEnemies = 0;
        int safety = 0;
        while (placedEnemies < desiredEnemies && safety < 500) {
            safety++;
            int x = rng.nextInt(width);
            int y = rng.nextInt(height);
            if ((x == startX && y == startY) || !dungeon.isWalkable(x, y) || dungeon.isOccupiedByEnemy(x, y)) continue;
            if (distanceManhattan(x, y, startX, startY) < 3) continue;
            int hp = 6 + rng.nextInt(7); // 6-12
            int atk = 2 + rng.nextInt(3); // 2-4
            char g = rng.nextBoolean() ? 'g' : 's'; // goblin or slime glyph
            dungeon.addEnemy(new Enemy(x, y, hp, atk, g));
            placedEnemies++;
        }

        // Place items randomly on walkable tiles
        int placedItems = 0;
        safety = 0;
        while (placedItems < desiredItems && safety < 500) {
            safety++;
            int x = rng.nextInt(width);
            int y = rng.nextInt(height);
            if (!dungeon.isWalkable(x, y) || (x == startX && y == startY)) continue;
            if (dungeon.getItemAt(x, y) != null) continue;
            Item.Type type = rng.nextBoolean() ? Item.Type.POTION : Item.Type.TREASURE;
            dungeon.addItem(new Item(x, y, type));
            placedItems++;
        }

        return dungeon;
    }

    // carveRoom: set all tiles in the rect to FLOOR
    private static void carveRoom(Dungeon d, Rect r) {
        for (int y = r.y; y < r.y + r.h; y++) {
            for (int x = r.x; x < r.x + r.w; x++) {
                d.setTile(x, y, Tile.FLOOR);
            }
        }
    }

    // carveCorridor: make an L-shaped corridor between two points, randomizing orientation
    private static void carveCorridor(Dungeon d, int x1, int y1, int x2, int y2) {
        if (new Random().nextBoolean()) {
            carveH(d, x1, x2, y1);
            carveV(d, y1, y2, x2);
        } else {
            carveV(d, y1, y2, x1);
            carveH(d, x1, x2, y2);
        }
    }

    // carveH: carve a horizontal line of FLOOR tiles between two x positions at y
    private static void carveH(Dungeon d, int x1, int x2, int y) {
        int from = Math.min(x1, x2);
        int to = Math.max(x1, x2);
        for (int x = from; x <= to; x++) {
            if (d.isInBounds(x, y)) d.setTile(x, y, Tile.FLOOR);
        }
    }

    // carveV: carve a vertical line of FLOOR tiles between two y positions at x
    private static void carveV(Dungeon d, int y1, int y2, int x) {
        int from = Math.min(y1, y2);
        int to = Math.max(y1, y2);
        for (int y = from; y <= to; y++) {
            if (d.isInBounds(x, y)) d.setTile(x, y, Tile.FLOOR);
        }
    }

    // connectRooms: connect a list of room rects by ordering them and carving corridors sequentially
    private static void connectRooms(Dungeon d, List<Rect> rooms, Random rng) {
        List<Rect> ordered = new ArrayList<>(rooms);
        Collections.sort(ordered, (a, b) -> Integer.compare(a.centerX() + a.centerY(), b.centerX() + b.centerY()));
        for (int i = 0; i < ordered.size() - 1; i++) {
            Rect r1 = ordered.get(i);
            Rect r2 = ordered.get(i + 1);
            carveCorridor(d, r1.centerX(), r1.centerY(), r2.centerX(), r2.centerY());
        }
    }

    // Manhattan distance helper
    private static int distanceManhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    // Small helper rect class used during generation to represent rooms
    private static final class Rect {
        final int x, y, w, h;
        Rect(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        boolean intersects(Rect other) {
            return x < other.x + other.w && x + w > other.x && y < other.y + other.h && y + h > other.y;
        }
        int centerX() { return x + w / 2; }
        int centerY() { return y + h / 2; }
    }
}
