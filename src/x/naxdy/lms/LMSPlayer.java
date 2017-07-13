package x.naxdy.lms;

import org.bukkit.entity.Player;

public class LMSPlayer
{
	private Player p;
	
	public LMSPlayer(Player p)
	{
		// this is a stub for now.
		// other values might be added to this, for example the selected kit.
		this.p = p;
	}
	
	public Player getPlayer()
	{
		return p;
	}
}
