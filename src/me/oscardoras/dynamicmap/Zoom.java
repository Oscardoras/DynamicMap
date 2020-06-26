package me.oscardoras.dynamicmap;

import org.bukkit.Location;

public class Zoom {
	
	private final int lenght;
	
	public Zoom(float pitch) {
		this.lenght = (int) ((3968f / 45f) * (90f - (pitch > 45f ? pitch : 45f)) + 128f);
	}
	
	public int getLength() {	//nombre de blocs affich�s sur la carte en fonction du zoom
		return lenght;
	}
	
	public int getSize() {	//nombre de chunks dans un MapChunk en fonction du zoom
		int lenght = getLength();
		if (lenght < 256) return 1;
		else if (lenght < 512) return 2;
		else if (lenght < 1024) return 4;
		else if (lenght < 2048) return 8;
		else return 16;
	}
	
	public int getChunkLength() {	//nombre de pixels d'un MapChunk
		int blockSize = getSize() * 16;	//nombre de blocs d'un MapChunk
		double pixel = getLength() / 128d;	//nombre de blocs que repr�sente un pixel
		return (int) (blockSize / pixel);	//nombre de pixels
	}
	
	public int getX(Location center, MapChunk chunk) {	//coordonn�e X sur la carte d'un MapChunk en fonction du zoom et du centre
		int worldX = chunk.x * chunk.size * 16;	//coordonn�e X en bloc du MapChunk
		int relativeX = worldX - center.getBlockX();	//distance X en bloc entre le MapChunk et le centre
		double pixel = getLength() / 128d;	//nombre de blocs que repr�sente un pixel
		return (int) (64 + (relativeX / pixel));	//nombre de pixels
	}
	
	public int getY(Location center, MapChunk chunk) {	//coordonn�e Y sur la carte d'un MapChunk en fonction du zoom et du centre
		int worldZ = chunk.z * chunk.size * 16;
		int relativeZ = worldZ - center.getBlockZ();
		double pixel = getLength() / 128d;
		return (int) (64 + (relativeZ / pixel));
	}
	
	public int getX(Location center, Location pointer) {	//coordonn�e X sur la carte d'un pointeur en fonction du zoom et du centre
		int relativeX = pointer.getBlockX() - center.getBlockX();	//distance X en bloc entre le pointeur et le centre
		double pixel = getLength() / 256d;	//nombre de blocs que repr�sente un pixel
		int positionX = (int) (relativeX / pixel);	//nombre de pixels
		positionX = positionX < -128 ? -128 : positionX;
		positionX = positionX > 127 ? 127 : positionX;
		return positionX;
	}
	
	public int getY(Location center, Location pointer) {	//coordonn�e Y sur la carte d'un pointeur en fonction du zoom et du centre
		int relativeZ = pointer.getBlockZ() - center.getBlockZ();	//distance Z en bloc entre le pointeur et le centre
		double pixel = getLength() / 256d;	//nombre de blocs que repr�sente un pixel
		int positionY = (int) (relativeZ / pixel);	//nombre de pixels
		positionY = positionY < -128 ? -128 : positionY;
		positionY = positionY > 127 ? 127 : positionY;
		return positionY;
	}
	
	@Override
	public boolean equals(Object object) {
		return object != null && object instanceof Zoom && lenght == ((Zoom) object).lenght;
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		hash *= 8 + lenght;
		return hash;
	}
	
}