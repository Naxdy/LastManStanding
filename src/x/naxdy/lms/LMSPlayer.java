package x.naxdy.lms;

import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import x.naxdy.hcmcpvp.HcmcCore;

public class LMSPlayer
{
	private Player p;
	private boolean saved;
	
	public LMSPlayer(Player p)
	{
		this.p = p;
		saved = false;
	}
	
	public Player getPlayer()
	{
		return p;
	}
	
	public void savePlayerStats(int rank, int maxPlayers)
	{
		if(saved)
			return;
		
		// TODO: honor
		rank = rank == 1 ? 0 : rank;
		int honor = (int)Math.round((((double)maxPlayers/2D) - rank) * 40D);
		if(p.isOnline())
		{
			ChatColor color = honor >= 0 ? ChatColor.GREEN : ChatColor.RED;
			String inf = (honor >= 0 ? "gained" : "lost");
			p.sendMessage(HcmcCore.formatStr(color, "You " + inf + " " + (honor > 0 ? honor : -honor) + " honor."));
		}
		try
		{
			if(rank == 0)
			{
				LastManStanding.getCore().incrementPlayerField(p.getUniqueId(), "sps_won");
			}
			LastManStanding.getCore().incrementPlayerField(p.getUniqueId(), "sps_played");
			LastManStanding.getCore().changeHonor(p.getUniqueId(), honor);
		}
		catch (SQLException e)
		{
			LastManStanding.getCore().dblogE("ERROR: Could not save honor for LMS Player!! See console for details.");
			e.printStackTrace();
		}
		
		saved = true;
	}
}
