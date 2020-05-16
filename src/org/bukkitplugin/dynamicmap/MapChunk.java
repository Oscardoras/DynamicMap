package org.bukkitplugin.dynamicmap;

import org.bukkit.Chunk;
import org.bukkit.World;

public class MapChunk {
	
	public final World world;
	public final int x;
	public final int z;
	public final int size;
	
	public MapChunk(World world, int x, int z, int size) {
		this.world = world;
		this.x = x;
		this.z = z;
		this.size = size;
	}
	
	public MapChunk(Chunk chunk) {
		this.world = chunk.getWorld();
		this.x = chunk.getX();
		this.z = chunk.getZ();
		this.size = 1;
	}
	
	public MapChunk getParent() {
		if (size == 16) return null;
		else return new MapChunk(world, (int) Math.floor(x / 2d), (int) Math.floor(z / 2d), size * 2);
	}
	
	public MapChunk[] getChildren() {
		if (size > 1) {
			MapChunk[] chunks = new MapChunk[4];
			chunks[0] = new MapChunk(world, (x * 2), (z * 2), size / 2);
			chunks[1] = new MapChunk(world, (x * 2), (z * 2) + 1, size / 2);
			chunks[2] = new MapChunk(world, (x * 2) + 1, (z * 2), size / 2);
			chunks[3] = new MapChunk(world, (x * 2) + 1, (z * 2) + 1, size / 2);
			return chunks;
		}
		return new MapChunk[0];
	}
	
	public MapChunk getWest() {
		return new MapChunk(world, x - 1, z, size);
	}
	
	public MapChunk getEast() {
		return new MapChunk(world, x + 1, z, size);
	}
	
	public MapChunk getNorth() {
		return new MapChunk(world, x, z - 1, size);
	}
	
	public MapChunk getSouth() {
		return new MapChunk(world, x, z + 1, size);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof MapChunk) {
			MapChunk o = (MapChunk) object;
			return world.equals(o.world) && x == o.x && z == o.z && size == o.size;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		hash *= 7 + world.hashCode();
		hash *= 10 + x;
		hash *= 19 + z;
		hash *= 8 + size;
		return hash;
	}
	
}