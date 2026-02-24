package net.fabricmc.example;

public class Bookmark {
    public int x;
    public int z;
    public String name;
    public int color;
    public boolean permanent;

    public Bookmark(int x, int z, String name, int color, boolean permanent) {
        this.x = x;
        this.z = z;
        this.name = name;
        this.color = color;
        this.permanent = permanent;
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%s,%d,%b", x, z, name, color, permanent);
    }

    public static Bookmark fromString(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 5)
                return null;
            int x = Integer.parseInt(parts[0]);
            int z = Integer.parseInt(parts[1]);
            String name = parts[2];
            int color = Integer.parseInt(parts[3]);
            boolean permanent = Boolean.parseBoolean(parts[4]);
            return new Bookmark(x, z, name, color, permanent);
        } catch (Exception e) {
            return null;
        }
    }
}
