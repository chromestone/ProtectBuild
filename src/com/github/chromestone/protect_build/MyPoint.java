package com.github.chromestone.protect_build;

import org.bukkit.Chunk;

import java.io.Serializable;

/**
 * The MyPoint class
 * Created by Derek Zhang on 8/6/19.
 */
public class MyPoint implements Serializable {

    private static final long serialVersionUID = -389399447051164051L;;

    final int x;
    final int y;

    public MyPoint(int x, int y) {

        this.x = x;
        this.y = y;
    }

    static MyPoint fromChunk(Chunk c) {

        return new MyPoint(c.getX(), c.getZ());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof MyPoint) {

            MyPoint p = (MyPoint) obj;
            return this.x == p.x && this.y == p.y;
        }
        return false;
    }

    // derived from Bukkit source code
    // see https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse
    @Override
    public int hashCode()
    {
        int hash = 7;

        hash = 79 * hash + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
        hash = 79 * hash + (int)(Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
        return hash;
    }

    @Override
    public String toString() {

        return "(" + x + "," + y + ")";
    }
}
