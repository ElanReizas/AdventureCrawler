package com.adventure.crawler;

import java.io.Console;
import java.util.Random;

// Entry point and main game loop.
// Handles loading/saving via SaveManager, bootstraps Dungeon and Player,
// processes user input, advances enemy turns, and renders state via Renderer.
public class Game {
    private static final int DEFAULT_WIDTH = 50;
    private static final int DEFAULT_HEIGHT = 22;

    // main: program entry. Loads save state if present, otherwise creates a new dungeon and player.
    // Runs the input loop (Console or System.in fallback), handles player actions, then enemy turns,
    // and re-renders after each step. On death writes high score and deletes save to enforce permadeath.
    public static void main(String[] args) throws Exception {
        boolean newRun = true;
        SaveManager.GameState loaded = SaveManager.load();
        long seed = System.currentTimeMillis();
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        Player player;
        Dungeon dungeon;

        // If a saved game exists, restore seed, size and player state
        if (loaded != null) {
            seed = loaded.seed;
            width = loaded.width;
            height = loaded.height;
            dungeon = DungeonGenerator.generate(width, height, seed, 10, 10);
            player = new Player(loaded.playerX, loaded.playerY);
            player.setHp(loaded.playerHp);
            player.setPotions(loaded.playerPotions);
            player.setTreasure(loaded.playerTreasure);
            newRun = false;
        } else {
            // Fresh run: generate a dungeon and place player at the start
            dungeon = DungeonGenerator.generate(width, height, seed, 10, 10);
            player = new Player(dungeon.getStartX(), dungeon.getStartY());
        }

        Random rng = new Random(seed);

        String message = newRun ? "Welcome to Adventure Crawler!" : "Loaded saved run.";
        Renderer.render(dungeon, player, message);

        // Use Console if available (nicer input), otherwise fallback to buffered System.in
        Console console = System.console();
        if (console == null) {
            // fallback: use System.in for environments without Console (IDE terminals)
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            while (player.isAlive()) {
                int c = br.read();
                if (c == -1) break;
                char key = (char) c;
                if (key == '\n' || key == '\r') continue;

                // handleInput applies the player's action and returns a message for the next render
                message = handleInput(key, player, dungeon, rng, seed);

                // Advance enemy behaviour after the player acts
                enemyTurns(dungeon, player, rng);

                // redraw the game state
                Renderer.render(dungeon, player, message);
                if (!player.isAlive()) break;
            }
        } else {
            // Console-based loop: read full lines and take the first character as command
            while (player.isAlive()) {
                String line = console.readLine("");
                if (line == null || line.isEmpty()) continue;
                char key = Character.toLowerCase(line.charAt(0));
                message = handleInput(key, player, dungeon, rng, seed);
                enemyTurns(dungeon, player, rng);
                Renderer.render(dungeon, player, message);
            }
        }

        // Game over: record high score and delete save to enforce permadeath
        int currentScore = player.getTreasure();
        int high = SaveManager.readHighScore();
        if (currentScore > high) SaveManager.writeHighScore(currentScore);
        System.out.println("\nYou died! Score: " + currentScore + "  High Score: " + Math.max(high, currentScore));
        java.io.File f = new java.io.File("saves/save.txt");
        if (f.exists()) f.delete();
    }

    // handleInput: maps keypresses to game actions.
    // Movement keys (WASD) call Player.moveBy; F attacks adjacent enemies; E drinks potion; Q saves and exits.
    // Returns a short message to display on the next render.
    private static String handleInput(char key, Player player, Dungeon dungeon, Random rng, long seed) {
        key = Character.toLowerCase(key);
        int dx = 0, dy = 0;
        switch (key) {
            case 'w' -> dy = -1;
            case 's' -> dy = 1;
            case 'a' -> dx = -1;
            case 'd' -> dx = 1;
            case 'f' -> { // attack into facing direction (simple: try each adjacent)
                StringBuilder sb = new StringBuilder();
                int[][] dirs = { {1,0}, {-1,0}, {0,1}, {0,-1} };
                int hits = 0;
                for (int[] dir : dirs) {
                    Enemy e = dungeon.getEnemyAt(player.getX()+dir[0], player.getY()+dir[1]);
                    if (e != null && e.isAlive()) { e.damage(5); hits++; }
                }
                return hits > 0 ? "You swing and hit " + hits + " foe(s)!" : "You swing at nothing.";
            }
            case 'e' -> {
                boolean did = player.drinkPotion();
                return did ? "You drink a potion and feel better." : "No potions to drink.";
            }
            case 'q' -> {
                // Save current game state and exit
                SaveManager.GameState s = new SaveManager.GameState();
                s.seed = seed;
                s.width = dungeon.getWidth(); s.height = dungeon.getHeight();
                s.playerX = player.getX(); s.playerY = player.getY();
                s.playerHp = player.getHp();
                s.playerPotions = player.getPotions();
                s.playerTreasure = player.getTreasure();
                SaveManager.save(s);
                System.out.println("Saved. Bye!");
                System.exit(0);
            }
            default -> {}
        }
        if (dx != 0 || dy != 0) {
            // Attempt to move the player; moveBy handles combat/loot on tile
            boolean acted = player.moveBy(dungeon, dx, dy);
            if (!acted) return "You bump into a wall.";
            return "";
        }
        return "";
    }

    // enemyTurns: iterate all enemies and let them perform their AI-driven actions for the turn.
    private static void enemyTurns(Dungeon dungeon, Player player, Random rng) {
        for (Enemy e : dungeon.getEnemies()) {
            e.takeTurn(dungeon, player, rng);
        }
    }
}
