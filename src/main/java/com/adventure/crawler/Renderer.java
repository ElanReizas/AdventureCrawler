package com.adventure.crawler;

import java.util.List;

// Renderer: responsible for drawing the entire game state to the console.
// It prints the visible map, player '@', enemy glyphs, item glyphs, and a HUD line with HP/potions/gold.
public class Renderer {
    // render: builds a textual representation of the dungeon line-by-line and writes it to stdout.
    // Uses simple ordering: player > enemy > item > tile glyph. Clears the terminal before printing.
    public static void render(Dungeon dungeon, Player player, String message) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < dungeon.getHeight(); y++) {
            for (int x = 0; x < dungeon.getWidth(); x++) {
                // Draw player on top
                if (player.getX() == x && player.getY() == y) {
                    sb.append('@');
                    continue;
                }
                // Draw enemy if present and alive
                Enemy e = dungeon.getEnemyAt(x, y);
                if (e != null && e.isAlive()) {
                    sb.append(e.getGlyph());
                    continue;
                }
                // Draw item if present
                Item item = dungeon.getItemAt(x, y);
                if (item != null) {
                    sb.append(item.getGlyph());
                    continue;
                }
                // Fallback: draw tile glyph
                sb.append(dungeon.getTile(x, y).glyph);
            }
            sb.append('\n');
        }
        // Append a single-line HUD with core player stats and control hints
        sb.append("HP:").append(player.getHp())
          .append("  Potions:").append(player.getPotions())
          .append("  Gold:").append(player.getTreasure())
          .append("  (WASD move, F attack, E drink, Q save+quit)\n");
        if (message != null && !message.isEmpty()) {
            sb.append(message).append('\n');
        }
        // ANSI clear screen sequence (works in many terminals) then print buffer
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.print(sb.toString());
    }
}
