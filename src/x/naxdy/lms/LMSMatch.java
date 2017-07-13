package x.naxdy.lms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import x.naxdy.hcmcpvp.HcmcCore;

public class LMSMatch
{
	private World world;
	private int maxPlayers;
	private int minPlayers;
	private Map<UUID, LMSPlayer> contestants;
	private int matchCounter;
	private int beginningPlayers;
	private LastManStanding lmsplugin;
	private int instanceCloseTimer;
	private boolean removeMatch;
	private boolean initMatchDone;
	private List<LMSEvent> eventsHappened;
	private int matchTime;
	private int feastY;
	private ItemStack compass;
	
	public LMSMatch(LastManStanding lmsplugin, int maxPlayers, int minPlayers)
	{
		this.maxPlayers = maxPlayers;
		this.minPlayers = minPlayers;
		this.lmsplugin = lmsplugin;
		eventsHappened = new ArrayList<LMSEvent>();
		matchCounter = 61;
		instanceCloseTimer = 11;
		beginningPlayers = 0;
		removeMatch = false;
		initMatchDone = false;
		matchTime = 0;
		feastY = 0;
		contestants = new HashMap<UUID, LMSPlayer>();
		
		compass = new ItemStack(Material.COMPASS);
		ItemMeta met = compass.getItemMeta();
		met.setDisplayName(ChatColor.RESET + "Tracking Compass");
		met.setLore(Arrays.asList(ChatColor.GRAY + "Right-click to show the position", ChatColor.GRAY + "of the closest player."));
		compass.setItemMeta(met);
		
		String worldName = "instance" + lmsplugin.getInstanceCountInc();
		
		boolean foundworld = false;
		do
		{
			Bukkit.unloadWorld(worldName, false);
			LastManStanding.removeDir(worldName);
			
			WorldCreator w = new WorldCreator(worldName);
			
			Bukkit.createWorld(w);
			
			world = Bukkit.getWorld(worldName);
			
			for(int x = -10; x < 10 && !foundworld; x++)
			{
				for(int z = -10; z < 10 && !foundworld; z++)
				{
					for(int y = 128; y > 30 && !foundworld; y--)
					{
						Material b = world.getBlockAt(x, y, z).getType();
						if(b != Material.AIR)
						{
							if(b != Material.WATER && b != Material.LAVA && b != Material.STATIONARY_LAVA && b != Material.STATIONARY_WATER)
							{
								foundworld = true;
							}
							else
							{
								y = 0;
							}
						}
					}
				}
			}
			
			if(!foundworld)
			{
				Bukkit.unloadWorld(worldName, false);
				LastManStanding.removeDir(worldName);
				worldName = "instance" + lmsplugin.getInstanceCountInc();
			}
		} while(!foundworld);
	}
	
	// called once per second
	public void update()
	{
		if(!hasStarted())
		{
			setWorldTime(0);
			
			if(contestants.size() >= minPlayers)
			{
				matchCounter--;
			}
			else
			{
				matchCounter = 61;
			}
			
			if((matchCounter == 60 || matchCounter == 30 || matchCounter <= 10) && matchCounter > 0)
			{
				broadcastMessage(ChatColor.YELLOW + "The match will start in " + HcmcCore.formatTime(matchCounter) + "!");
			}
		}
		else
		{
			matchTime++;
			// GAME ROUTINE
			if(!initMatchDone)
			{
				ArrayList<LMSPlayer> ps = getAllContestants();
				for(LMSPlayer p : ps)
				{
					p.getPlayer().teleport(getRandomSpawnLocation());
					initPlayer(p.getPlayer());
				}
				
				broadcastMessage(ChatColor.GREEN + "The match has begun! Everyone is invulnerable for 1 minute.");
				initMatchDone = true;
			}
			
			if((getMatchTime() == 30 || getMatchTime() >= LMSEvent.INVULN_DONE.getTimeAt()-10) && getMatchTime() < LMSEvent.INVULN_DONE.getTimeAt())
			{
				broadcastMessage(ChatColor.RED + "Invulnerability will fade in " + HcmcCore.formatTime(LMSEvent.INVULN_DONE.getTimeAt()-(int)getMatchTime()));
			}
			
			if(getMatchTime() >= LMSEvent.INVULN_DONE.getTimeAt() && !eventsHappened.contains(LMSEvent.INVULN_DONE))
			{
				broadcastMessage(ChatColor.RED + "You are now vulnerable.\n" + ChatColor.GRAY + "This instance has been locked. New players may no longer join, and if you leave, you will lose.");
				beginningPlayers = contestants.size();
				eventsHappened.add(LMSEvent.INVULN_DONE);
			}
			
			if(instanceCloseTimer == 11)
			{
				if(getMatchTime() >= LMSEvent.FEAST_SPAWN.getTimeAt() - LMSEvent.FEAST_PREPARE.getTimeAt() && !eventsHappened.contains(LMSEvent.FEAST_PREPARE))
				{
					prepareFeastSpawn();
					eventsHappened.add(LMSEvent.FEAST_PREPARE);
				}
				
				int feastIn = (int) (LMSEvent.FEAST_SPAWN.getTimeAt() - getMatchTime());
				if((feastIn == 300 || feastIn == 120 || feastIn == 60 || feastIn == 30 || feastIn <= 10) && feastIn > 0)
				{
					broadcastMessage(ChatColor.YELLOW + "Chests containing diamond gear will spawn at X:0 Z:0 in " + HcmcCore.formatTime(feastIn) + "!");
				}
				else if(feastIn <= 0 && !eventsHappened.contains(LMSEvent.FEAST_SPAWN))
				{
					broadcastMessage(ChatColor.GREEN + "The chests have spawned, get them!");
					setFeast();
					eventsHappened.add(LMSEvent.FEAST_SPAWN);
				}
			}
			
			// WINNER
			if(contestants.size() == 1 && instanceCloseTimer == 11 && eventsHappened.contains(LMSEvent.INVULN_DONE))
			{
				LMSPlayer winner = getAllContestants().get(0);
				broadcastMessage(ChatColor.GREEN + winner.getPlayer().getDisplayName() + ChatColor.GREEN + " has won! A new match will start in 10 seconds.");
				winner.savePlayerStats(contestants.size(), beginningPlayers);
				instanceCloseTimer--;
			}
			
			if(contestants.size() <= 1 && instanceCloseTimer < 11)
			{
				instanceCloseTimer--;
			}
		}
		
		// TODO: other match criteria.?
		if(instanceCloseTimer <= 0)
		{
			endMatch();
		}
	}
	
	public boolean allocatePlayer(Player p)
	{
		if(!canJoin() || p.isOp())
			return false;
		
		contestants.put(p.getUniqueId(), new LMSPlayer(p));
		p.teleport(getRandomSpawnLocation());
		p.setGameMode(GameMode.SURVIVAL);
		if(contestants.size() < minPlayers)
			p.sendMessage(HcmcCore.formatStr(ChatColor.AQUA, "The match will start once at least " + minPlayers + " players have joined.\nPlease wait, or play another gamemode at our /hub"));
		
		broadcastMessage(HcmcCore.formatStr(ChatColor.YELLOW, p.getDisplayName() + ChatColor.GRAY + " joined the match."));
		
		initPlayer(p);
		
		return true;
	}
	
	public boolean printPlayers(Player p)
	{
		if(!isInWorld(p))
			return false;
		
		if(!hasStarted())
		{
			p.sendMessage(HcmcCore.formatStr(ChatColor.RED, "The game hasn't started yet!"));
			return true;
		}
		else if(!eventsHappened.contains(LMSEvent.INVULN_DONE))
		{
			p.sendMessage(HcmcCore.formatStr(ChatColor.RED, "You can't do that while everyone is invulnerable."));
			return true;
		}
		else
		{
			List<LMSPlayer> remainingPlayers = getAllContestants();
			String plrs = "";
			for(int i = 0; i < remainingPlayers.size(); i++)
			{
				plrs += remainingPlayers.get(i).getPlayer().getDisplayName() + ChatColor.GRAY + ", ";
			}
			
			plrs = plrs.substring(0, plrs.length()-2);
			
			p.sendMessage(HcmcCore.formatStr(ChatColor.AQUA, "Remaining Players [" + remainingPlayers.size() + "/" + beginningPlayers + "]:\n" + plrs));
			return true;
		}
	}
	
	public boolean onPlayerChat(AsyncPlayerChatEvent event)
	{
		if(isInWorld(event.getPlayer()))
		{
			boolean spec = event.getPlayer().getGameMode() == GameMode.SPECTATOR && !event.getPlayer().isOp();
			if(spec)
			{
				if(LastManStanding.getCore().getPremiumCode(event.getPlayer()) < 1)
				{
					event.getPlayer().sendMessage(HcmcCore.formatStr(ChatColor.RED, "Only premium members may chat while spectating.\nBecome a premium member at http://hcmcpvp.com/"));
				}
				else
				{
					broadcastMessage(ChatColor.GRAY + "<" + event.getPlayer().getDisplayName() + ChatColor.GRAY + "> " + ChatColor.GRAY + event.getMessage(), spec);
					LastManStanding.getLMSLogger().info("[" + world.getName() + "] (SPEC)" + "<" + event.getPlayer().getDisplayName() + "> " + event.getMessage());
				}
			}
			else
			{
				broadcastMessage("<" + event.getPlayer().getDisplayName() + "> " + event.getMessage(), spec);
				LastManStanding.getLMSLogger().info("[" + world.getName() + "] " + "<" + event.getPlayer().getDisplayName() + "> " + event.getMessage());
			}
			
			event.setCancelled(true);
		}
		
		return false;
	}
	
	public boolean onPlayerQuit(PlayerQuitEvent event)
	{
		if(getLMSPlayer(event.getPlayer()) == null)
			return false;
		
		if(hasStarted() && eventsHappened.contains(LMSEvent.INVULN_DONE))
		{
			broadcastMessage(HcmcCore.formatStr(ChatColor.AQUA, event.getPlayer().getDisplayName() + ChatColor.GRAY + " has been eliminated due to disconnection!\nPlayers remaining: " + (contestants.size()-1)));
			getLMSPlayer(event.getPlayer()).savePlayerStats(contestants.size(), beginningPlayers);
		}
		else
			broadcastMessage(HcmcCore.formatStr(ChatColor.YELLOW, event.getPlayer().getDisplayName() + ChatColor.GRAY + " has left the match."));
		
		removeLMSPlayer(event.getPlayer());
		return true;
	}
	
	public boolean onEntityDamage(EntityDamageEvent event)
	{
		if(!(event.getEntity() instanceof Player))
		{
			if(world.getEntities().contains(event.getEntity()))
			{
				if(!hasStarted() || instanceCloseTimer != 11 || contestants.size() == 1)
					event.setCancelled(true);
				
				return true;
			}
			return false;
		}
		else if(isInMatch((Player)event.getEntity()))
		{
			if(!hasStarted() || !eventsHappened.contains(LMSEvent.INVULN_DONE))
			{
				event.setCancelled(true);
			}
			return true;
		}
		
		return false;
	}
	
	public boolean onPlayerMove(PlayerMoveEvent event)
	{
		if(!isInMatch(event.getPlayer()))
			return false;
		
		if(!hasStarted())
		{
			initPlayer(event.getPlayer());
		}
		
		return true;
	}
	
	public boolean onBlockBreak(BlockBreakEvent event)
	{
		if(isInMatch(event.getPlayer()))
		{
			if(!hasStarted())
			{
				event.getPlayer().sendMessage(HcmcCore.formatStr(ChatColor.RED, "The match hasn't started yet!"));
				event.setCancelled(true);
			}
			else if(event.getBlock().getType() == Material.DIAMOND_ORE)
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage(HcmcCore.formatStr(ChatColor.RED, "A mysterious force forbids you to mine this item."));
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean onPlayerInteract(PlayerInteractEvent event)
	{
		if(isInMatch(event.getPlayer()))
		{
			if(!hasStarted())
			{
				event.setCancelled(true);
			}
			else
			{
				if((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
						
						((event.getHand() == EquipmentSlot.HAND && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.COMPASS) ||
						(event.getHand() == EquipmentSlot.OFF_HAND && event.getPlayer().getInventory().getItemInOffHand().getType() == Material.COMPASS)))
				{
					trackPlayer(event.getPlayer());
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean onPlayerDeath(PlayerDeathEvent event)
	{
		if(!isInMatch(event.getEntity()))
			return false;
		
		broadcastMessage(HcmcCore.formatStr(ChatColor.AQUA, event.getEntity().getDisplayName() + ChatColor.GRAY + " has been eliminated! Players remaining: " + (contestants.size()-1)));
		
		event.getEntity().setGameMode(GameMode.SPECTATOR);
		event.getEntity().sendMessage(HcmcCore.formatStr(ChatColor.RED, "You have been eliminated at rank " + ChatColor.YELLOW + contestants.size() + ChatColor.GRAY + ". You may continue spectating, or join another match by typing " + ChatColor.YELLOW + "/match"));
		removeLMSPlayer(event.getEntity());
		
		event.setDeathMessage("");
		
		return true;
	}
	
	private void initPlayer(Player p)
	{
		p.setFoodLevel(20);
		p.setHealth(20);
		p.setSaturation(0);
		p.setExhaustion(4);
		p.getInventory().clear();
		p.getInventory().setHelmet(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setBoots(null);
		p.getInventory().setItemInOffHand(null);
		
		if(hasStarted())
		{
			p.getInventory().addItem(compass);
			p.setCompassTarget(new Location(world, 0, 0, 0));
		}
	}
	
	private boolean trackPlayer(Player tracker)
	{
		if(!isInMatch(tracker))
			return false;
		
		if(contestants.size() == 1)
			tracker.sendMessage(HcmcCore.formatStr(ChatColor.AQUA, "You're the only one!"));
		/*else if(contestants.size() == 2)
		{
			if(getAllContestants().get(0).getPlayer().equals(tracker))
			{
				tracker.setCompassTarget(getAllContestants().get(1).getPlayer().getLocation());
				tracker.sendMessage(HcmcCore.formatStr(ChatColor.AQUA, "Your compass is pointing at " + getAllContestants().get(1).getPlayer().getDisplayName() + ChatColor.GRAY + "."));
			}
			else
			{
				tracker.setCompassTarget(getAllContestants().get(0).getPlayer().getLocation());
				tracker.sendMessage(HcmcCore.formatStr(ChatColor.AQUA, "Your compass is pointing at " + getAllContestants().get(0).getPlayer().getDisplayName() + ChatColor.GRAY + "."));
			}
		}*/
		else
		{
			List<LMSPlayer> others = getAllContestants();
			Player target = null;
			
			double closestDistance = 0;
			double minDistance = 10D;
			
			for(int i = 0; i < others.size(); i++)
			{
				if(!others.get(i).getPlayer().equals(tracker))
				{
					double distance = tracker.getLocation().distance(others.get(i).getPlayer().getLocation());
					
					if((target == null || distance < closestDistance) && distance > minDistance)
					{
						closestDistance = distance;
						target = others.get(i).getPlayer();
					}
				}
			}
			
			if(target == null)
			{
				tracker.sendMessage(HcmcCore.formatStr(ChatColor.RED, "Everyone else is near you!"));
			}
			else
			{
				tracker.sendMessage(HcmcCore.formatStr(ChatColor.AQUA, "Closest Player: " + target.getDisplayName() + ChatColor.GRAY + ". Position: X:" + target.getLocation().getBlockX() + " Z:" + target.getLocation().getBlockZ()));
			}
		}
		
		return true;
	}
	
	private void prepareFeastSpawn()
	{
		feastY = 0;
		for(int i = 256; i > 30 && feastY == 0; i--)
		{
			Material b = world.getBlockAt(0, i, 0).getType();
			if(b != Material.AIR && b != Material.LEAVES && b != Material.LEAVES_2 && b != Material.VINE && b != Material.LOG && b != Material.LOG_2)
			{
				if(b != Material.WATER && b != Material.STATIONARY_WATER && b != Material.LAVA && b != Material.STATIONARY_LAVA)
					feastY = i-5;
				else
					feastY = i;
			}
		}
		
		LastManStanding.getLMSLogger().info("Should prepare feast at Y " + feastY);
		
		for(int x = -20; x <= 20; x++)
		{
			for(int z = -20; z <= 20; z++)
			{
				if(Math.pow(x, 2) + Math.pow(z, 2) <= Math.pow(20, 2))
				{
					for(int y = feastY; y < feastY + 20; y++)
					{
						if(y == feastY)
							world.getBlockAt(x, y, z).setType(Material.GRASS);
						else
							world.getBlockAt(x, y, z).setType(Material.AIR);
					}
				}
			}
		}
	}
	
	private void setFeast()
	{
		for(int x = -2; x <= 2; x++)
		{
			for(int z = -2; z <= 2; z++)
			{
				if((Math.abs(x) == 2 && z == 0) ||
						(Math.abs(z) == 2 && x == 0) ||
						(Math.abs(x) == 1 && Math.abs(z) == 1))
				{
					world.getBlockAt(x, feastY+1, z).setType(Material.CHEST);
					List<ItemStack> cItems = LMSFeast.getFeastChestItems();
					for(ItemStack it : cItems)
					{
						((Chest)world.getBlockAt(x, feastY+1, z).getState()).getBlockInventory().addItem(it);
					}
					world.getBlockAt(x, feastY+1, z).getState().update();
				}
			}
		}
	}
	
	private void endMatch()
	{
		// this should probably just contain the rank 1 player, but better safe than sorry.
		ArrayList<LMSPlayer> finalContestants = getAllContestants();
		for(int i = 0; i < finalContestants.size(); i++)
		{
			finalContestants.get(i).savePlayerStats(contestants.size(), beginningPlayers);
		}
		
		for(Player p : world.getPlayers())
		{
			if(!p.isOp())
				lmsplugin.allocatePlayer(p);
			else
				p.teleport(Bukkit.getWorld("world").getSpawnLocation());
		}
		
		Bukkit.unloadWorld(world, false);
		LastManStanding.removeDir(world.getName());
		
		removeMatch = true;
	}
	
	private void broadcastMessage(String message)
	{
		broadcastMessage(message, false);
	}
	
	private void broadcastMessage(String message, boolean spectatorOnly)
	{
		for(Player p : world.getPlayers())
		{
			if((spectatorOnly && (p.getGameMode() == GameMode.SPECTATOR || p.isOp())) || !spectatorOnly)
				p.sendMessage(message);
		}
	}
	
	public boolean isInMatch(Player p)
	{
		return contestants.containsKey(p.getUniqueId());
	}
	
	public boolean isInWorld(Player p)
	{
		return world.getPlayers().contains(p);
	}
	
	public boolean removeMatch()
	{
		return removeMatch;
	}
	
	private ArrayList<LMSPlayer> getAllContestants()
	{
		ArrayList<LMSPlayer> ret = new ArrayList<LMSPlayer>();
		Iterator<Entry<UUID, LMSPlayer>> it = contestants.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry<UUID, LMSPlayer> pair = (Map.Entry<UUID, LMSPlayer>)it.next();
			ret.add(pair.getValue());
		}
		
		return ret;
	}
	
	private long getMatchTime()
	{
		return matchTime;
	}
	
	private void setWorldTime(long time)
	{
		if(world != null)
			world.setTime(time);
	}
	
	private boolean canJoin()
	{
		return ((!hasStarted() || !eventsHappened.contains(LMSEvent.INVULN_DONE)) && contestants.size() < maxPlayers && instanceCloseTimer == 11);
	}
	
	private boolean hasStarted()
	{
		return matchCounter <= 0;
	}
	
	private void removeLMSPlayer(Player p)
	{
		removeLMSPlayer(p.getUniqueId());
	}
	
	private void removeLMSPlayer(UUID p)
	{
		if(eventsHappened.contains(LMSEvent.INVULN_DONE))
			contestants.get(p).savePlayerStats(contestants.size(), beginningPlayers);
		
		contestants.remove(p);
	}
	
	private LMSPlayer getLMSPlayer(Player p)
	{
		return getLMSPlayer(p.getUniqueId());
	}
	
	private LMSPlayer getLMSPlayer(UUID p)
	{
		return contestants.get(p);
	}
	
	private Location getRandomSpawnLocation()
	{
		boolean foundLoc = false;
		int x = 0;
		int z = 0;
		int y = 0;
		int tries = 0;
		do
		{
			x = (int)(Math.random() * 50) * (Math.random() > 0.5 ? -1 : 1);
			z = (int)(Math.random() * 50) * (Math.random() > 0.5 ? -1 : 1);
			y = 0;
			for(int i = 256; i > 30 && y == 0; i--)
			{
				Material b = world.getBlockAt(x,  i,  z).getType();
				if(b != Material.AIR)
				{
					if((b != Material.WATER && b != Material.LAVA && b != Material.STATIONARY_LAVA && b != Material.STATIONARY_WATER) || tries >= 50)
					{
						foundLoc = true;
						y = i+1;
					}
					else
					{
						i = 0;
						foundLoc = false;
					}
				}
			}
			tries++;
		}
		while(!foundLoc);
		
		Location loc = new Location(world, x, y, z);
		
		return loc;
	}
}
