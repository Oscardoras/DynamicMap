package org.bukkitplugin.dynamicmap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class ChunkRenderer {
	
	private static final Map<Integer, Set<MapChunk>> mapChunksToRender = new HashMap<Integer, Set<MapChunk>>();
	private static final List<MapCache> chunkCache = new ArrayList<MapCache>();
	public static final Map<String, Color> colorCache = new HashMap<String, Color>();
	
	static {
		mapChunksToRender.put(1, new HashSet<MapChunk>());
		mapChunksToRender.put(2, new HashSet<MapChunk>());
		mapChunksToRender.put(4, new HashSet<MapChunk>());
		mapChunksToRender.put(8, new HashSet<MapChunk>());
		mapChunksToRender.put(16, new HashSet<MapChunk>());
	}
	
	public static void run() {
		for (int size : new Integer[] {1, 2, 4, 8, 16}) {
			MapChunk chunk = null;
			synchronized(mapChunksToRender) {
				Set<MapChunk> mapChunks = mapChunksToRender.get(size);
				Iterator<MapChunk> it = mapChunks.iterator();
				if (it.hasNext()) {
					chunk = it.next();
					it.remove();
				}
			}
			if (chunk != null) {
				if (size == 1) renderChunk(chunk);
				else renderOver(chunk);
				break;
			}
		}
		
		synchronized(chunkCache) {
    		Iterator<MapCache> it = chunkCache.iterator();
    		while (it.hasNext()) if (it.next().get() == null) it.remove();
		}
	}
	
	private static void renderChunk(MapChunk chunk) {
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		
		Chunk c = chunk.world.getChunkAt(chunk.x, chunk.z);
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 255; y >= 0; y--) {
					Block block = c.getBlock(x, y, z);
					if (block != null) {
						Material material = block.getType();
						if (material != Material.AIR) {
							String id = material.name().toLowerCase();
							if (colorCache.containsKey(id)) {
								float coef = 0.5f + (y / 510f);
								
								Color color = colorCache.get(id);
								image.setRGB(x, z, new Color((int) (color.getRed() * coef), (int) (color.getGreen() * coef), (int) (color.getBlue() * coef)).getRGB());
								
								break;
							}
						}
					}
				}
			}
		}
		
		save(chunk, image);
		
		render(chunk.getParent());
	}
	
	private static void renderOver(MapChunk chunk) {
		BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gra = img.createGraphics();
		
		MapChunk[] children = chunk.getChildren();
		gra.drawImage(getImage(children[0]), 0, 0, null);
		gra.drawImage(getImage(children[1]), 0, 16, null);
		gra.drawImage(getImage(children[2]), 16, 0, null);
		gra.drawImage(getImage(children[3]), 16, 16, null);
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		image.createGraphics().drawImage(img.getScaledInstance(16, 16, Image.SCALE_DEFAULT), 0, 0, null);
		
		save(chunk, image);
		
		MapChunk parent = chunk.getParent();
		if (parent != null) render(parent);
	}
	
	private static Image getImage(MapChunk chunk) {
		Image image = getChunkRender(chunk);
		return image != null ? image : new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	}
	
	public static void render(MapChunk chunk) {
		synchronized(mapChunksToRender) {
			mapChunksToRender.get(chunk.size).add(chunk);
		}
	}
	
	public static Image getChunkRender(MapChunk chunk) {
		try {
			Image image = null;
	    	synchronized(chunkCache) {
	    		MapCache mapCache = new MapCache(chunk, null);
    			if (chunkCache.contains(mapCache)) image = chunkCache.get(chunkCache.indexOf(mapCache)).get();
    			if (image == null) {
    				image = ImageIO.read(new File(chunk.world.getWorldFolder().getPath() + "/dynamicmap/" + chunk.size + "/" + chunk.x + "_" + chunk.z + ".png"));
    				chunkCache.remove(mapCache);
    				chunkCache.add(new MapCache(chunk, image));
    			}
	    	}
			return image;
		} catch (Exception e) {
			return null;
		}
	}
	
	private static void save(MapChunk chunk, BufferedImage image) {
	    synchronized(chunkCache) {
	    	MapCache mapCache = new MapCache(chunk, image);
    		chunkCache.remove(mapCache);
    		chunkCache.add(mapCache);
	    }
		try {
			File file = new File(chunk.world.getWorldFolder().getPath() + "/dynamicmap/" + chunk.size + "/" + chunk.x + "_" + chunk.z + ".png");
			file.setReadable(true);
			file.setWritable(true);
			if (!file.isFile()) {
				file.mkdirs();
				file.delete();
				file.createNewFile();
			}
			ImageIO.write(image, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

class MapCache extends SoftReference<Image> {
	
	public MapChunk mapChunk;

	public MapCache(MapChunk mapChunk, Image image) {
		super(image);
		this.mapChunk = mapChunk;
	}
	
	@Override
	public boolean equals(Object object) {
		return object != null && object instanceof MapCache && mapChunk.equals(((MapCache) object).mapChunk);
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		hash *= 3 + mapChunk.hashCode();
		return hash;
	}
	
}