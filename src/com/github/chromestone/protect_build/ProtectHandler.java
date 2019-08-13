package com.github.chromestone.protect_build;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ProtectHandler class
 * Created by Derek Zhang on 8/6/19.
 */
public class ProtectHandler {

    private static final int REGION_SIZE = 512;
    private static final HashMap<Object, Object> SENTINEL = new HashMap<>();

    private final String dataDir;

    private final ExecutorService executor;
    private final ConcurrentHashMap<MyPoint, HashMap<Object, Object>> data;

    public ProtectHandler(String dataDir) {

        this.dataDir = dataDir;

        executor = Executors.newFixedThreadPool(5 * Runtime.getRuntime().availableProcessors());
        data = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public void loadChunk(Chunk c, final Logger logger) {

        final MyPoint point = MyPoint.fromChunk(c);
        executor.submit(() -> {

            int regionX = point.x / REGION_SIZE, regionY = point.y / REGION_SIZE;
            Path path = Paths.get(dataDir, regionX + "." + regionY, point.x + "." + point.y + ".bin");
            File file = path.toFile();

            if (!file.isDirectory()) {

                logger.log(Level.SEVERE, "cannot load chunk [{0}]: file already exists as directory", point);
                return;
            }

            if(file.exists()) {

                try (FileInputStream fis = new FileInputStream(file);
                     ObjectInputStream ois = new ObjectInputStream(fis)) {

                    Object obj = ois.readObject();
                    if (obj instanceof HashMap) {

                        HashMap<Object, Object> map = (HashMap<Object, Object>) obj;
                        data.put(point, map);
                    }
                    else {

                        logger.log(Level.SEVERE,
                                   "cannot load chunk [{0}]: file does not deserialize to a hash-map", point);
                    }
                }
                catch (Exception e) {

                    logger.log(Level.SEVERE, "cannot load chunk [" + point + "] unable to deserialize", e);
                }
            }
            else {

                data.put(point, SENTINEL);
            }
        });
    }

    public void unloadChunk(Chunk c) {

        //TODO
        data.remove(MyPoint.fromChunk(c));
    }

    public boolean setBlockOwner(Block b, Integer identity) {//Player p, Block b) {

        MyPoint point = MyPoint.fromChunk(b.getChunk());
        HashMap<Object, Object> owners = data.get(point);
        if (owners == SENTINEL) {

            owners = new HashMap<>();
            data.put(point, owners);
        }
        owners.put(new Vector(b.getX(), b.getY(), b.getZ()), identity);

        return true;
    }

    public Optional<Boolean> isBlockOwner(Block b, Integer identity) {//Player p, Block b) {

        HashMap<Object, Object> owners = data.get(MyPoint.fromChunk(b.getChunk()));

        if (owners == SENTINEL) {

            return Optional.of(true);
        }
        else if (owners == null) {

            return Optional.empty();
        }

        Object owner = owners.get(new Vector(b.getX(), b.getY(), b.getZ()));

        if (owner == null) {

            return Optional.of(true);
        }

        return Optional.of(owner.equals(identity));
    }
}
