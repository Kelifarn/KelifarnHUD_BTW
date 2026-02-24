package net.fabricmc.example;

import java.io.*;
import java.util.*;

public class Holder {
    public static boolean autoFishEnabled = false;
    public static Random rand = null;

    // Map & Bookmark data
    public static Set<Long> exploredChunks = new HashSet<Long>();
    public static List<Bookmark> bookmarks = new ArrayList<Bookmark>();

    public static long getChunkKey(int x, int z) {
        return ((long) (x >> 4) << 32) | ((z >> 4) & 0xFFFFFFFFL);
    }

    public static void save(File worldDir) {
        if (worldDir == null)
            return;
        File hudDir = new File(worldDir, "kelifarn_hud");
        if (!hudDir.exists())
            hudDir.mkdirs();

        // Save chunks
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(new File(hudDir, "chunks.dat")));
            for (Long key : exploredChunks) {
                writer.println(key);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save bookmarks
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(new File(hudDir, "bookmarks.dat")));
            for (Bookmark b : bookmarks) {
                writer.println(b.toString());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(File worldDir) {
        exploredChunks.clear();
        bookmarks.clear();
        if (worldDir == null)
            return;
        File hudDir = new File(worldDir, "kelifarn_hud");
        if (!hudDir.exists())
            return;

        // Load chunks
        File chunkFile = new File(hudDir, "chunks.dat");
        if (chunkFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(chunkFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        exploredChunks.add(Long.parseLong(line.trim()));
                    } catch (NumberFormatException e) {
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load bookmarks
        File bookmarkFile = new File(hudDir, "bookmarks.dat");
        if (bookmarkFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(bookmarkFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    Bookmark b = Bookmark.fromString(line);
                    if (b != null)
                        bookmarks.add(b);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
