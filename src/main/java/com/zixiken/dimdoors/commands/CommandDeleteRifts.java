package com.zixiken.dimdoors.commands;

import java.util.ArrayList;

import com.zixiken.dimdoors.DimDoors;
import com.zixiken.dimdoors.core.DimLink;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import com.zixiken.dimdoors.core.NewDimData;
import com.zixiken.dimdoors.core.PocketManager;
import com.zixiken.dimdoors.util.Point4D;

public class CommandDeleteRifts extends DDCommandBase
{
	private static CommandDeleteRifts instance = null;

	private CommandDeleteRifts()
	{
		super("dd-deleterifts", "[dimension number]");
	}

	public static CommandDeleteRifts instance()
	{
		if (instance == null)
			instance = new CommandDeleteRifts();

		return instance;
	}

	@Override
	protected DDCommandResult processCommand(EntityPlayer sender, String[] command)
	{
		int linksRemoved = 0;
		int targetDimension;

		if (command.length > 1)
		{
			return DDCommandResult.TOO_MANY_ARGUMENTS;
		}
		if (command.length == 0)
		{
			targetDimension = sender.worldObj.provider.dimensionId;
		}
		else
		{
			try
			{
				targetDimension = Integer.parseInt(command[0]);
			}
			catch (NumberFormatException e) 
			{
				return DDCommandResult.INVALID_DIMENSION_ID;
			}
		}

		World world = PocketManager.loadDimension(targetDimension);
		if (world == null)
		{
			return DDCommandResult.UNREGISTERED_DIMENSION;
		}
		
		int x;
		int y;
		int z;
		Point4D location;
		NewDimData dimension = PocketManager.createDimensionData(world);
		ArrayList<DimLink> links = dimension.getAllLinks();
		for (DimLink link : links)
		{
			location = link.source();
			x = location.getX();
			y = location.getY();
			z = location.getZ();
			if (world.getBlock(x, y, z) == DimDoors.blockRift)
			{
				// Remove the rift and its link
				world.setBlockToAir(x, y, z);
				dimension.deleteLink(link);
				linksRemoved++;
			}
			else if (!DimDoors.blockRift.isBlockImmune(world, x, y, z))
			{
				// If a block is not immune, then it must not be a DD block.
				// The link would regenerate into a rift eventually.
				// We only need to remove the link.
				dimension.deleteLink(link);
				linksRemoved++;
			}
		}
		sendChat(sender, "Removed " + linksRemoved + " links.");
		return DDCommandResult.SUCCESS;
	}
}