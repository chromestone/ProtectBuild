package com.github.chromestone.protect_build;

import org.bukkit.*;
import org.bukkit.block.Block;

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
    private final ConcurrentHashMap<My2DPoint, HashMap<Object, Object>> data;

    public ProtectHandler(String dataDir) {

        this.dataDir = dataDir;

        //TODO make threads configurable
        executor = Executors.newFixedThreadPool(5 * Runtime.getRuntime().availableProcessors());
        data = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public void loadChunk(Chunk c, final Logger logger) {

        if (executor.isShutdown()) {

            return;
        }

        final My2DPoint point = My2DPoint.fromChunk(c);
        executor.submit(() -> {

            int regionX = point.x / REGION_SIZE, regionY = point.y / REGION_SIZE;
            Path path = Paths.get(dataDir, regionX + "." + regionY, point.x + "." + point.y + ".bin");
            File file = path.toFile();

            if (file.isDirectory()) {

                logger.log(Level.SEVERE,
                           "cannot load data for chunk [{0}]: file already exists as directory", point);
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
                                   "cannot load data for chunk [{0}]: file does not deserialize to a hash-map",
                                   point);
                    }
                }
                catch (Exception e) {

                    logger.log(Level.SEVERE,
                               "cannot load data for chunk [" + point + "]: unable to deserialize", e);
                }
            }
            else {

                data.put(point, SENTINEL);
            }
        });
    }

    private void saveMap(My2DPoint point, HashMap<Object, Object> map, Logger logger) {

        int regionX = point.x / REGION_SIZE, regionY = point.y / REGION_SIZE;
        Path path = Paths.get(dataDir, regionX + "." + regionY, point.x + "." + point.y + ".bin");
        File file = path.toFile();

        if (file.isDirectory()) {

            logger.log(Level.SEVERE,
                       "cannot save data for chunk [{0}]: file already exists as directory", point);
            return;
        }

        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {

            boolean success = parentFile.mkdirs();

            if (!success) {

                logger.log(Level.SEVERE,
                           "cannot save data for chunk [{0}]: unable to create parent directories", point);
                return;
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file, false);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(map);
        }
        catch (Exception e) {

            logger.log(Level.SEVERE, "cannot save data for chunk [\" + point + \"]: unable to serialize", e);
        }
    }

    public void unloadChunk(Chunk c, final Logger logger) {

        if (executor.isShutdown()) {

            return;
        }

        final My2DPoint point = My2DPoint.fromChunk(c);
        executor.submit(() -> {

            HashMap<Object, Object> map = data.remove(point);

            if (map == SENTINEL) {

                return;
            }

            if (map == null) {

                logger.log(Level.WARNING, "cannot save data for chunk [{0}]: map is null", point);
                return;
            }

            saveMap(point, map, logger);
        });
    }

    public void save(Logger logger) {

        if (executor.isShutdown()) {

            return;
        }

        final ConcurrentHashMap.KeySetView<My2DPoint, HashMap<Object, Object>> keys = data.keySet();

        for (final My2DPoint point : keys) {

            executor.submit(() -> {

                // no need to remove since teardown in progress
                HashMap<Object, Object> map = data.get(point);

                if (map == SENTINEL || map == null) {

                    return;
                }

                saveMap(point, map, logger);
            });
        }
    }

    /**
     * Once you call this, don't use this class again! Make the reference null if you want.
     * @param logger The logger object to use for logging.
     */
    public void finalSave(Logger logger) {

        logger.log(Level.INFO,
                   "Attempting to save data for chunks... (can take up to 1 minute) please be patient.");

        save(logger);

        try {

            executor.shutdown();
            boolean result = executor.awaitTermination(1L, TimeUnit.MINUTES);
            if (!result) {

                logger.log(Level.SEVERE,
                           "unable to finish saving data for chunks in time, loss of grief protection possible");
            }
        }
        catch (InterruptedException e) {

            logger.log(Level.SEVERE,
                       "wait on saving threads was interrupted, loss of grief protection possible", e);
        }

        executor.shutdownNow();
    }

    public boolean setBlockOwner(Block b, Integer identity) {

        My2DPoint point = My2DPoint.fromChunk(b.getChunk());
        //Bukkit.broadcastMessage("set: " + identity + " | " + point + " | " + My3DPoint.fromBlock(b));
        HashMap<Object, Object> owners = data.get(point);
        if (owners == SENTINEL) {

            owners = new HashMap<>();
            data.put(point, owners);
        }
        else if (owners == null) {

            return false;
        }

        owners.put(My3DPoint.fromBlock(b), identity);

        return true;
    }

    public void removeBlockOwner(Block b) {

        My2DPoint point = My2DPoint.fromChunk(b.getChunk());
        //Bukkit.broadcastMessage("remove: " + point.toString());
        HashMap<Object, Object> owners = data.get(point);
        if (owners != SENTINEL) {

            owners.remove(My3DPoint.fromBlock(b));
        }
    }

    public Optional<Boolean> isBlockOwner(Block b, Integer identity) {

        //Bukkit.broadcastMessage("check: " + identity + " | " +
        //        My2DPoint.fromChunk(b.getChunk()) + " | " + My3DPoint.fromBlock(b));

        HashMap<Object, Object> owners = data.get(My2DPoint.fromChunk(b.getChunk()));

        if (owners == SENTINEL) {

            return Optional.of(true);
        }
        else if (owners == null) {

            return Optional.empty();
        }

        Object owner = owners.get(My3DPoint.fromBlock(b));

        if (owner == null) {

            return Optional.of(true);
        }

        return Optional.of(owner.equals(identity));
    }
}
