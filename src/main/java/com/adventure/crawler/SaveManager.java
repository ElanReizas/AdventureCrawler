package com.adventure.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// SaveManager: handles simple file-based save/load of a minimal GameState and a high score value.
// Format is plain text with one value per line in a fixed order. Also ensures save directory exists.
public class SaveManager {
    private static final String SAVE_DIR = "saves";
    private static final String SAVE_FILE = SAVE_DIR + "/save.txt";
    private static final String SCORE_FILE = SAVE_DIR + "/highscore.txt";

    // Ensure the save directory exists on disk
    public static void ensureSaveDir() {
        new File(SAVE_DIR).mkdirs();
    }

    // save: write the GameState to a text file with one value per line (seed, width, height, playerX, playerY, playerHp, playerPotions, playerTreasure)
    public static void save(GameState state) {
        ensureSaveDir();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            bw.write(Long.toString(state.seed)); bw.newLine();
            bw.write(Integer.toString(state.width)); bw.newLine();
            bw.write(Integer.toString(state.height)); bw.newLine();
            bw.write(Integer.toString(state.playerX)); bw.newLine();
            bw.write(Integer.toString(state.playerY)); bw.newLine();
            bw.write(Integer.toString(state.playerHp)); bw.newLine();
            bw.write(Integer.toString(state.playerPotions)); bw.newLine();
            bw.write(Integer.toString(state.playerTreasure)); bw.newLine();
        } catch (IOException e) {
            System.err.println("Failed to save: " + e.getMessage());
        }
    }

    // load: read the saved GameState from disk. Returns null if no save exists or parsing fails.
    public static GameState load() {
        ensureSaveDir();
        File f = new File(SAVE_FILE);
        if (!f.exists()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            long seed = Long.parseLong(br.readLine());
            int width = Integer.parseInt(br.readLine());
            int height = Integer.parseInt(br.readLine());
            int px = Integer.parseInt(br.readLine());
            int py = Integer.parseInt(br.readLine());
            int php = Integer.parseInt(br.readLine());
            int ppot = Integer.parseInt(br.readLine());
            int ptre = Integer.parseInt(br.readLine());
            GameState s = new GameState();
            s.seed = seed; s.width = width; s.height = height;
            s.playerX = px; s.playerY = py; s.playerHp = php; s.playerPotions = ppot; s.playerTreasure = ptre;
            return s;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load: " + e.getMessage());
            return null;
        }
    }

    // writeHighScore: store top score as a single integer in a file
    public static void writeHighScore(int score) {
        ensureSaveDir();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SCORE_FILE))) {
            bw.write(Integer.toString(score));
        } catch (IOException e) {
            System.err.println("Failed to write high score: " + e.getMessage());
        }
    }

    // readHighScore: return stored high score or 0 if not present/parse error
    public static int readHighScore() {
        ensureSaveDir();
        File f = new File(SCORE_FILE);
        if (!f.exists()) return 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            return Integer.parseInt(br.readLine());
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    // GameState: compact serializable state used by save/load
    public static class GameState {
        public long seed;
        public int width;
        public int height;
        public int playerX;
        public int playerY;
        public int playerHp;
        public int playerPotions;
        public int playerTreasure;
    }
}
