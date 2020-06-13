package org.bukkitplugin.dynamicmap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class ChunkRenderer {
	
	private static final Map<Integer, Queue<MapChunk>> toRender = new HashMap<Integer, Queue<MapChunk>>();
	private static final Map<MapChunk, SoftReference<Image>> cache = new HashMap<MapChunk, SoftReference<Image>>();
	public static final Map<String, Color> colorCache = new HashMap<String, Color>();
	
	static {
		toRender.put(1, new ConcurrentLinkedQueue<MapChunk>());
		toRender.put(2, new ConcurrentLinkedQueue<MapChunk>());
		toRender.put(4, new ConcurrentLinkedQueue<MapChunk>());
		toRender.put(8, new ConcurrentLinkedQueue<MapChunk>());
		toRender.put(16, new ConcurrentLinkedQueue<MapChunk>());
	}
	
	public static void run() {
		for (int size : new Integer[] {1, 2, 4, 8, 16}) {
			Queue<MapChunk> mapChunks = toRender.get(size);
			MapChunk chunk = mapChunks.peek();
			if (chunk != null) {
				if (size == 1) renderChunk(chunk);
				else renderOver(chunk);
			}
			mapChunks.poll();
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
		gra.drawImage(getChunkRender(children[0]), 0, 0, null);
		gra.drawImage(getChunkRender(children[1]), 0, 16, null);
		gra.drawImage(getChunkRender(children[2]), 16, 0, null);
		gra.drawImage(getChunkRender(children[3]), 16, 16, null);
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		image.createGraphics().drawImage(img.getScaledInstance(16, 16, Image.SCALE_DEFAULT), 0, 0, null);
		
		save(chunk, image);
		
		MapChunk parent = chunk.getParent();
		if (parent != null) render(parent);
	}
	
	private static void save(MapChunk mapChunk, BufferedImage image) {
	    synchronized(cache) {
	    	cache.put(mapChunk, new SoftReference<Image>(image));
	    }
		try {
			File file = new File(mapChunk.world.getWorldFolder().getPath() + "/dynamicmap/" + mapChunk.size + "/" + mapChunk.x + "_" + mapChunk.z + ".png");
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
	
	public static void render(MapChunk chunk) {
		toRender.get(chunk.size).offer(chunk);
	}
	
	public static Image getChunkRender(MapChunk mapChunk) {
		Image image = null;
		try {
	    	synchronized(cache) {
    			if (cache.containsKey(mapChunk)) image = cache.get(mapChunk).get();
    			if (image == null) {
    				image = ImageIO.read(new File(mapChunk.world.getWorldFolder().getPath() + "/dynamicmap/" + mapChunk.size + "/" + mapChunk.x + "_" + mapChunk.z + ".png"));
    				cache.put(mapChunk, new SoftReference<Image>(image));
    			}
	    	}
		} catch (Exception e) {}
		if (image == null) {
			image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			image.getGraphics().setColor(new Color(0, 0, 0, 0));
		}
		return image;
	}
	
}