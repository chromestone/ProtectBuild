package com.github.chromestone.protect_build;

import org.bukkit.block.Block;

import java.io.Serializable;

/**
 * The My3DPoint class
 * Created by Derek Zhang on 8/17/19.
 */
public class My3DPoint implements Serializable {

    private static final long serialVersionUID = -3985671422828374378L;

    final int x;
    final int y;
    final int z;

    public My3DPoint(int x, int y, int z) {

        this.x = x;
        this.y = y;
        this.z = z;
    }

    static My3DPoint fromBlock(Block b) {

        return new My3DPoint(b.getX(), b.getY(), b.getZ());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof My3DPoint) {

            My3DPoint p = (My3DPoint) obj;
            return this.x == p.x && this.y == p.y && this.z == p.z;
        }
        return false;
    }

    // copied from Bukkit source code
    // see https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse
    @Override
    public int hashCode()  {

        int hash = 7;

        hash = 79 * hash + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
        hash = 79 * hash + (int)(Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    @Override
    public String toString() {

        return "(" + x + "," + y + "," + z + ")";
    }
}
