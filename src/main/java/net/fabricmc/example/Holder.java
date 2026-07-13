package net.fabricmc.example;

import net.minecraft.src.*;
import java.io.*;
import java.util.*;

public class Holder {
    public static boolean autoFishEnabled = false;
    public static Random rand = null;

    // Map & Bookmark data
    public static Set<Long> exploredChunks = new HashSet<Long>();
    public static List<Bookmark> bookmarks = new ArrayList<Bookmark>();
    public static File currentWorldDir = null;

    // Ability Statistics
    public static int miningXP = 0;
    public static int miningLevel = 0;
    public static int miningPoints = 0;

    public static int fightingXP = 0;
    public static int fightingLevel = 0;
    public static int fightingPoints = 0;

    public static int survivalXP = 0;
    public static int survivalLevel = 0;
    public static int survivalPoints = 0;

    // Active Upgrade Perks
    public static int upgradeMiningSpeed = 0;       // Max 3 (Fast Miner)

    // Helper states
    public static int lastHealth = -1;
    public static int lastFood = -1;
    public static float originalGamma = -1.0f;
    public static long adrenalineTimer = 0;
    public static Map<Integer, Long> lastAttackedMobs = new HashMap<Integer, Long>();

    public static long getChunkKey(int x, int z) {
        return ((long) (x >> 4) << 32) | ((z >> 4) & 0xFFFFFFFFL);
    }

    public static void saveBookmarks() {
        if (currentWorldDir != null) {
            save(currentWorldDir);
        }
    }

    public static int getRequiredXP(int level) {
        return 100 * (level/5 + 1);
    }

    public static void addMiningXP(int amount) {
        miningXP += amount;
        int req = getRequiredXP(miningLevel);
        if (miningXP >= req) {
            miningXP -= req;
            miningLevel++;
            miningPoints++;
            Minecraft.getMinecraft().thePlayer.addChatMessage("\u00a7a[Abilities] Mining Level Up! Level " + miningLevel + " (\u00a7e+1 Point\u00a7a)");
        }
        saveBookmarks(); // Save state
    }

    public static void addFightingXP(int amount) {
        fightingXP += amount;
        int req = getRequiredXP(fightingLevel);
        if (fightingXP >= req) {
            fightingXP -= req;
            fightingLevel++;
            fightingPoints++;
            Minecraft.getMinecraft().thePlayer.addChatMessage("\u00a7c[Abilities] Fighting Level Up! Level " + fightingLevel + " (\u00a7e+1 Point\u00a7c)");
        }
        saveBookmarks(); // Save state
    }

    public static void addSurvivalXP(int amount) {
        survivalXP += amount;
        int req = getRequiredXP(survivalLevel);
        if (survivalXP >= req) {
            survivalXP -= req;
            survivalLevel++;
            survivalPoints++;
            Minecraft.getMinecraft().thePlayer.addChatMessage("\u00a7b[Abilities] Survival Level Up! Level " + survivalLevel + " (\u00a7e+1 Point\u00a7b)");
        }
        saveBookmarks(); // Save state
    }

    public static void onBlockBroken() {
        addMiningXP(1);
    }

    public static void onMobDeath(EntityLivingBase lastAttacker) {
        addFightingXP(1);
    }

    public static void save(File worldDir) {
        if (worldDir == null)
            return;
        currentWorldDir = worldDir;
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

        // Save abilities
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(new File(hudDir, "abilities.dat")));
            writer.println("miningXP:" + miningXP);
            writer.println("miningLevel:" + miningLevel);
            writer.println("miningPoints:" + miningPoints);
            writer.println("fightingXP:" + fightingXP);
            writer.println("fightingLevel:" + fightingLevel);
            writer.println("fightingPoints:" + fightingPoints);
            writer.println("survivalXP:" + survivalXP);
            writer.println("survivalLevel:" + survivalLevel);
            writer.println("survivalPoints:" + survivalPoints);

            writer.println("upgradeMiningSpeed:" + upgradeMiningSpeed);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(File worldDir) {
        exploredChunks.clear();
        bookmarks.clear();
        miningXP = 0;
        miningLevel = 0;
        miningPoints = 0;
        fightingXP = 0;
        fightingLevel = 0;
        fightingPoints = 0;
        survivalXP = 0;
        survivalLevel = 0;
        survivalPoints = 0;
        upgradeMiningSpeed = 0;
        lastHealth = -1;
        lastFood = -1;
        originalGamma = -1.0f;
        adrenalineTimer = 0;
        lastAttackedMobs.clear();

        if (worldDir == null)
            return;
        currentWorldDir = worldDir;
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

        // Load abilities
        File abilityFile = new File(hudDir, "abilities.dat");
        if (abilityFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(abilityFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length < 2) continue;
                    String key = parts[0].trim();
                    try {
                        int val = Integer.parseInt(parts[1].trim());
                        if (key.equals("miningXP")) miningXP = val;
                        else if (key.equals("miningLevel")) miningLevel = val;
                        else if (key.equals("miningPoints")) miningPoints = val;
                        else if (key.equals("fightingXP")) fightingXP = val;
                        else if (key.equals("fightingLevel")) fightingLevel = val;
                        else if (key.equals("fightingPoints")) fightingPoints = val;
                        else if (key.equals("survivalXP")) survivalXP = val;
                        else if (key.equals("survivalLevel")) survivalLevel = val;
                        else if (key.equals("survivalPoints")) survivalPoints = val;
                        else if (key.equals("upgradeMiningSpeed")) upgradeMiningSpeed = val;
                    } catch (NumberFormatException e) {
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
