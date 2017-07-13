package x.naxdy.lms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LMSFeast
{
	// TODO: pull items from config?
	private static List<ItemStack> possibleItems = Arrays.asList(new ItemStack(Material.DIAMOND_SWORD),
			new ItemStack(Material.DIAMOND_HELMET),
			new ItemStack(Material.DIAMOND_LEGGINGS),
			new ItemStack(Material.DIAMOND_CHESTPLATE),
			new ItemStack(Material.DIAMOND_BOOTS),
			new ItemStack(Material.GOLDEN_APPLE, 4),
			new ItemStack(Material.LAVA_BUCKET),
			new ItemStack(Material.POTION, 1, (short)8233),
			new ItemStack(Material.POTION, 1, (short)8226),
			new ItemStack(Material.POTION, 1, (short)16420),
			new ItemStack(Material.POTION, 1, (short)16426),
			new ItemStack(Material.POTION, 1, (short)8227));
	
	public static List<ItemStack> getFeastChestItems()
	{
		List<ItemStack> ret = new ArrayList<ItemStack>();
		Collections.shuffle(possibleItems);
		Queue<ItemStack> potentials = new LinkedBlockingQueue<ItemStack>();
		potentials.addAll(possibleItems);
		
		for(int i = 0; (Math.random() <= Math.pow(0.95, i-2) || i < 2) && potentials.size() > 0; i++)
		{
			ret.add(potentials.poll());
		}
		
		return ret;
	}

}
