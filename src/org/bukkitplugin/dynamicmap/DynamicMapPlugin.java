package org.bukkitplugin.dynamicmap;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkitutils.BukkitPlugin;
import org.bukkitutils.io.ConfigurationFile;

public class DynamicMapPlugin extends BukkitPlugin implements Listener {
	
	public static DynamicMapPlugin plugin;
	
	public DynamicMapPlugin() {
		plugin = this;
	}
	
	
	public MapView map;
	public Map<Player, Location> locations = new HashMap<Player, Location>();
	
	public void onEnable() {
		if (!getConfig().contains("ticks_per_rendering")) {
			getConfig().set("ticks_per_rendering", 100);
			saveConfig();
		}
		
		int ticks = getConfig().getInt("ticks_per_rendering");
		new Thread(() -> {
			while (Thread.currentThread().isAlive()) {
				try {
					Thread.sleep(20l * ticks);
					ChunkRenderer.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		registersColors();
		
		for (World world : Bukkit.getWorlds())
			for (Chunk chunk : world.getLoadedChunks())
				if (!new File(chunk.getWorld().getWorldFolder().getPath() + "/dynamicmap/1/" + chunk.getX() + "_" + chunk.getZ() + ".png").exists()) ChunkRenderer.render(new MapChunk(chunk));
		
		
		ConfigurationFile config = new ConfigurationFile(Bukkit.getWorlds().get(0).getWorldFolder().getPath() + "/data/dynamicmap.yml");
		if (!config.contains("map_id")) {
			map = Bukkit.createMap(Bukkit.getWorlds().get(0));
			config.set("map_id", map.getId());
			config.save();
		} else map = getMap(config.getInt("map_id"));
		map.getRenderers().clear();
		map.addRenderer(new Renderer());
		map.setWorld(Bukkit.getWorlds().get(0));
		
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			try {
				Iterator<Recipe> recipes = Bukkit.recipeIterator();
				while (recipes.hasNext()) {
					ItemStack item = recipes.next().getResult();
					if (item.getType() == Material.FILLED_MAP && ((MapMeta) item.getItemMeta()).getMapView().equals(map)) return;
				}
				ItemStack result = new ItemStack(Material.FILLED_MAP);
				MapMeta meta = (MapMeta) result.getItemMeta();
				meta.setMapView(map);
				result.setItemMeta(meta);
				
				ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "dynamicmap"), result);
				recipe.shape("XXX", "X#X", "XXX");
				recipe.setIngredient('X', Material.PAPER);
				recipe.setIngredient('#', Material.ENDER_EYE);
				Bukkit.addRecipe(recipe);
			} catch (Exception e) {}
		}, 0L, 20L);
		
		
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	private void registersColors() {
		for (Material material : Material.values()) if (material.isBlock()) {
			String id = material.name().toLowerCase();
			
			boolean b = true;
			if (id.endsWith("_button")) b = false;
			if (id.startsWith("potted_")) b = false;
			if (id.contains("skull")) b = false;
			if (id.contains("head")) b = false;
			if (id.contains("air")) b = false;
			if (id.contains("void")) b = false;
			if (id.equals("barrier")) b = false;
			if (id.equals("bubble_column")) b = false;
			
			if (b) {
				Color color;
				
				if (material == Material.WATER) {
					color = new Color(42, 105, 148);
				} else if (id.contains("grass")) {
					color = new Color(126, 178, 55);
				} else if (id.contains("leaves")) {
					color = new Color(0, 123, 0);
				} else {
					String name = id;
					
					name = name.startsWith("lava") ? "lava_still" : name;
					name = name.contains("fire") ? "fire_0" : name;
					name = name.equals("bamboo") ? "bamboo_large_leaves" : name;
					name = name.equals("bamboo_sapling") ? "bamboo_small_leaves" : name;
					name = name.equals("dried_kelp_block") ? "dried_kelp_top" : name;
					name = name.contains("chest") ? "oak_planks" : name;
					name = name.contains("redstone") ? "redstone" : name;
					name = name.contains("torch") ? "fire_0" : name;
					name = name.contains("snow") ? "snow" : name;
					name = name.contains("ice") ? "ice" : name;
					name = name.equals("heavy_weighted_pressure_plate") ? "iron_block" : name;
					name = name.equals("light_weighted_pressure_plate") ? "gold_block" : name;
					name = name.equals("magma_block") ? "lava_still" : name;
					
					name = name.startsWith("stripped_") ? name.replace("stripped_", "") : name;
					name = name.startsWith("petrified_") ? name.replace("petrified_", "") : name;
					name = name.endsWith("_wood") ? name.replace("_wood", "_log") : name;
					name = name.endsWith("_door") ? name.replace("_door", "") : name;
					name = name.endsWith("_trapdoor") ? name.replace("_trapdoor", "") : name;
					name = name.endsWith("_pressure_plate") ? name.replace("_pressure_plate", "") : name;
					name = name.endsWith("_fence") ? name.replace("_fence", "") : name;
					name = name.endsWith("_fence_gate") ? name.replace("_fence_gate", "") : name;
					name = name.endsWith("_sign") ? name.replace("_sign", "") : name;
					name = name.endsWith("_wall") ? name.replace("_wall", "") : name;
					name = name.endsWith("_slab") ? name.replace("_slab", "") : name;
					name = name.endsWith("_stairs") ? name.replace("_stairs", "") : name;
					name = name.startsWith("infested_") ? name.replace("infested_", "") : name;
					name = name.startsWith("smooth_") ? name.replace("smooth_", "") : name;
					
					try {
						color = getColor(name);
					} catch (Exception e) {
						try {
							color = getColor(name + "_top");
						} catch (Exception e1) {
							try {
								color = getColor(name + "_block");
							} catch (Exception e2) {
								try {
									color = getColor(name + "_block_top");
								} catch (Exception e3) {
									try {
										color = getColor(name + "_planks");
									} catch (Exception e4) {
										try {
											color = getColor(name + "s");
										} catch (Exception e5) {
											try {
												color = getColor(name + "_stage0");
											} catch (Exception e6) {
												try {
													color = getColor(name.split("_")[0] + "_wool");
												} catch (Exception e7) {
													try {
														color = getColor(name.split("_")[1] + "_wool");
													} catch (Exception e8) {
														name = material.isBurnable() ? "oak_planks" : "cobblestone";
														try {
															color = getColor(name);
														} catch (Exception e9) {
															color = null;
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				
				if (color != null) ChunkRenderer.colorCache.put(id, color);
			}
		}
	}
	
	private Color getColor(String name) throws IOException {
		try {
			return getColor(ImageIO.read(new File(getDataFolder().getPath() + "/resourcepack/assets/minecraft/textures/block/" + name + ".png")));
		} catch (Exception e) {
			return getColor(ImageIO.read(getResource("block/" + name + ".png")));
		}
	}
	
	private Color getColor(BufferedImage image) {
		int i = 0;
		int r = 0;
		int g = 0;
		int b = 0;
		for (int x = 0; x < image.getWidth(); x ++) {
			for (int y = 0; y < image.getHeight(); y ++) {
				Color color = new Color(image.getRGB(x, y), true);
				if (color.getAlpha() > 0) {
					i++;
					r += color.getRed();
					g += color.getGreen();
					b += color.getBlue();
				}
			}
		}
		return i > 0 ? new Color(r / i, g / i, b / i) : null;
	}
	
	@EventHandler
	public void on(PlayerInteractEvent e) {
		Action action = e.getAction();
		if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
			if (e.hasItem()) {
				ItemStack item = e.getItem();
				if (item.getType() == Material.FILLED_MAP) {
					ItemMeta meta = item.getItemMeta();
					if (meta != null && meta instanceof MapMeta && ((MapMeta) meta).getMapView().equals(map)) {
						Player player = e.getPlayer();
						if (!player.isSneaking()) {
							if (!locations.containsKey(player)) locations.put(player, player.getLocation());
							else locations.remove(player);
							e.setCancelled(true);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onLoad(ChunkLoadEvent e) {
		Chunk chunk = e.getChunk();
		if (!new File(chunk.getWorld().getWorldFolder().getPath() + "/dynamicmap/1/" + chunk.getX() + "_" + chunk.getZ() + ".png").exists()) ChunkRenderer.render(new MapChunk(chunk));
	}
	
	@EventHandler
	public void onBlock(BlockBreakEvent e) {
		reload(e);
	}
	@EventHandler
	public void onBlock(BlockBurnEvent e) {
		reload(e);
	}
	@EventHandler
	public void onBlock(BlockExplodeEvent e) {
		reload(e);
	}
	@EventHandler
	public void onBlock(BlockPlaceEvent e) {
		reload(e);
	}
	
	private void reload(BlockEvent e) {
		ChunkRenderer.render(new MapChunk(e.getBlock().getChunk()));
	}
	
	@SuppressWarnings("deprecation")
	public static MapView getMap(int id) {
		return Bukkit.getMap(id);
	}
	
}
