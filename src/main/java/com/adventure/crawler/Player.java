package com.adventure.crawler;

// Represents the player character and encapsulates position, HP, inventory (potions), and treasure.
public class Player {
    // Coordinates in dungeon
    private int x;
    private int y;
    // Current hit points
    private int hp;
    // Damage dealt when attacking
    private int attack;
    // Number of healing potions carried
    private int potions;
    // Collected treasure (score)
    private int treasure;

    // Create a player at given coordinates with default stats
    public Player(int x, int y) {
        this.x = x; this.y = y;
        this.hp = 20;
        this.attack = 5;
        this.potions = 0;
        this.treasure = 0;
    }

    // Simple getters used throughout the game to query player state
    public int getX() { return x; }
    public int getY() { return y; }
    public int getHp() { return hp; }
    public int getPotions() { return potions; }
    public int getTreasure() { return treasure; }
    public boolean isAlive() { return hp > 0; }

    // Apply damage to player
    public void damage(int amount) { hp -= amount; }

    // Heal the player but cap at max HP (20)
    public void heal(int amount) { hp = Math.min(20, hp + amount); }

    // Used when loading a save to set HP explicitly
    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(20, hp));
    }

    // Set number of potions (used when loading)
    public void setPotions(int potions) {
        this.potions = Math.max(0, potions);
    }

    // Set treasure count (used when loading)
    public void setTreasure(int treasure) {
        this.treasure = Math.max(0, treasure);
    }

    // moveBy attempts to move the player by dx,dy. If the target tile contains a living enemy,
    // the player attacks that enemy instead of moving. If the move succeeds and a collectible item
    // is present, the player picks it up (potions/treasure) and the item is removed from dungeon.
    // Returns true if an action occurred (move or attack), false if blocked by a wall.
    public boolean moveBy(Dungeon dungeon, int dx, int dy) {
        int nx = x + dx;
        int ny = y + dy;
        if (!dungeon.isWalkable(nx, ny)) return false;
        Enemy target = dungeon.getEnemyAt(nx, ny);
        if (target != null && target.isAlive()) {
            target.damage(attack);
            return true;
        }
        x = nx; y = ny;
        Item item = dungeon.getItemAt(x, y);
        if (item != null) {
            switch (item.getType()) {
                case POTION -> potions++;
                case TREASURE -> treasure++;
            }
            dungeon.removeItem(item);
        }
        return true;
    }

    // Drink a potion if available and heal the player for a fixed amount.
    public boolean drinkPotion() {
        if (potions <= 0) return false;
        potions--;
        heal(8);
        return true;
    }
}
