package me.oscardoras.dynamicmap;

import java.awt.Image;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

public class Renderer extends MapRenderer {

	@Override
	public void render(MapView map, MapCanvas canvas, Player player) {
		Location location = player.getLocation();
		if (DynamicMapPlugin.plugin.locations.containsKey(player)) {
			Location loc = DynamicMapPlugin.plugin.locations.get(player);
			location = location.add(new Vector(location.getX() - loc.getX(), location.getY() - loc.getY(), location.getZ() - loc.getZ()).multiply(new Zoom(loc.getPitch()).getLength()));
		}
		
		Zoom zoom = new Zoom(location.getPitch());
		int size = zoom.getSize();
		int chunkLength = zoom.getChunkLength();
		
		MapChunk chunk = new MapChunk(location.getChunk());
		while (chunk.size < size) chunk = chunk.getParent();
		while (zoom.getX(location, chunk) > 0) chunk = chunk.getWest();
		while (zoom.getY(location, chunk) > 0) chunk = chunk.getNorth();
		
		for (int y; (y = zoom.getY(location, chunk)) < 128; chunk = chunk.getSouth()) {
			MapChunk chunk2 = chunk;
			for (int x; (x = zoom.getX(location, chunk2)) < 128; chunk2 = chunk2.getEast()) {
				Image render = ChunkRenderer.getChunkRender(chunk2);
				canvas.drawImage(x, y, render.getScaledInstance(chunkLength, chunkLength, java.awt.Image.SCALE_DEFAULT));
			}
		}
		
		MapCursorCollection cursors = canvas.getCursors();
		for (int i = 0; i < cursors.size(); i++) cursors.removeCursor(cursors.getCursor(0));
		int pointerX = zoom.getX(location, location);
		int pointerY = zoom.getY(location, location);
		if (pointerX != -128 && pointerX != 127 && pointerY != -128 && pointerY != 127) {
			float a = location.getYaw() + 10;
			if (a < 0) a = 360 + a;
			if (a > 360) a = a - 360;
			cursors.addCursor(pointerX, pointerY, (byte) Math.round(a * 0.04166f));
		} else {
			cursors.addCursor(new MapCursor((byte) pointerX, (byte) pointerY,(byte) 0, MapCursor.Type.WHITE_CIRCLE, true));
		}
	}

}