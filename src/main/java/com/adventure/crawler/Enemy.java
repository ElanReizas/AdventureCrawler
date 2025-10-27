package com.adventure.crawler;

import java.util.Random;

// Enemy represents a simple AI-controlled opponent with position, HP, attack power and a glyph for rendering.
// It contains logic to chase the player when nearby, wander otherwise, and attack when adjacent.
public class Enemy {
    private int x;
    private int y;
    private int hp;
    private int attack;
    private final char glyph;

    // Construct an Enemy with position, hit points, attack damage and display character
    public Enemy(int x, int y, int hp, int attack, char glyph) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.attack = attack;
        this.glyph = glyph;
    }

    // Basic getters used by the renderer and game logic
    public int getX() { return x; }
    public int getY() { return y; }
    public int getHp() { return hp; }
    public boolean isAlive() { return hp > 0; }
    public char getGlyph() { return glyph; }

    // Apply damage to the enemy
    public void damage(int amount) {
        this.hp -= amount;
    }

    // takeTurn: very simple AI. If adjacent to the player, attack. If within chase range (manhattan <= 8),
    // attempt to move closer prioritizing the larger axis; otherwise pick a small random step to wander.
    private boolean tryMove(Dungeon dungeon, int nx, int ny) {
        if (dungeon.isWalkable(nx, ny) && !dungeon.isOccupiedByEnemy(nx, ny)) {
            this.x = nx; this.y = ny;
            return true;
        }
        return false;
    }

    public void takeTurn(Dungeon dungeon, Player player, Random rng) {
        if (!isAlive()) return;
        int dx = player.getX() - x;
        int dy = player.getY() - y;
        int dist = Math.abs(dx) + Math.abs(dy);

        // If adjacent, attack the player
        if (dist == 1) {
            player.damage(attack);
            return;
        }

        int stepX = Integer.compare(dx, 0);
        int stepY = Integer.compare(dy, 0);

        boolean moved = false;
        // If within chase range, try to move towards the player.
        if (dist <= 8) {
            // Prefer the axis with greater distance to close the gap efficiently
            if (Math.abs(dx) >= Math.abs(dy)) {
                moved = tryMove(dungeon, x + stepX, y);
                if (!moved) moved = tryMove(dungeon, x, y + stepY);
            } else {
                moved = tryMove(dungeon, x, y + stepY);
                if (!moved) moved = tryMove(dungeon, x + stepX, y);
            }
        }

        // If not moved by chasing logic, wander randomly (small step, avoid diagonals)
        if (!moved) {
            int[] choices = new int[] { -1, 0, 1 };
            int rx = choices[rng.nextInt(3)];
            int ry = choices[rng.nextInt(3)];
            if (Math.abs(rx) != Math.abs(ry)) { // avoid diagonal
                tryMove(dungeon, x + rx, y + ry);
            }
        }
    }
}
