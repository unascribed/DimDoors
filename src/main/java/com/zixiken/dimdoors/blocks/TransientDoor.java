package com.zixiken.dimdoors.blocks;

import com.zixiken.dimdoors.DimDoors;
import com.zixiken.dimdoors.core.DimLink;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import com.zixiken.dimdoors.core.DDTeleporter;
import com.zixiken.dimdoors.core.LinkType;
import com.zixiken.dimdoors.core.NewDimData;
import com.zixiken.dimdoors.core.PocketManager;

public class TransientDoor extends BaseDimDoor {
	public static final String ID = "transientDoor";

	public TransientDoor() {
		super(Material.iron);
		setHardness(1.0F);
		setUnlocalizedName(ID);
	}

	@Override
	public void enterDimDoor(World world, int x, int y, int z, Entity entity) 
	{
		// We need to ignore particle entities
		if (world.isRemote)
		{
			return;
		}

		// Check that this is the top block of the door
		if (world.getBlock(x, y - 1, z) == this)
		{
			boolean canUse = true;
			int metadata = world.getBlockMetadata(x, y - 1, z);
			if (canUse && entity instanceof EntityPlayer)
			{
				// Don't check for non-living entities since it might not work right
				canUse = isEntityFacingDoor(metadata, (EntityLivingBase) entity);
			}
			if (canUse)
			{
				// Teleport the entity through the link, if it exists
				DimLink link = PocketManager.getLink(x, y, z, world.provider.dimensionId);
				if (link != null)
				{
                    if (link.linkType() != LinkType.PERSONAL || entity instanceof EntityPlayer) {
                        DDTeleporter.traverseDimDoor(world, link, entity, this);
                        // Turn the door into a rift AFTER teleporting the player.
                        // The door's orientation may be necessary for the teleport.
                        world.setBlock(x, y, z, DimDoors.blockRift);
                        world.setBlockToAir(x, y - 1, z);
                    }
				}
			}
		}
		else if (world.getBlock(x, y + 1, z) == this)
		{
			enterDimDoor(world, x, y + 1, z, entity);
		}
	}	

	@Override
	public void placeLink(World world, int x, int y, int z) 
	{
		if (!world.isRemote && world.getBlock(x, y - 1, z) == this)
		{
			NewDimData dimension = PocketManager.createDimensionData(world);
			DimLink link = dimension.getLink(x, y, z);
			if (link == null && dimension.isPocketDimension())
			{
				dimension.createLink(x, y, z, LinkType.SAFE_EXIT,world.getBlockMetadata(x, y - 1, z));
			}
		}
	}
	
	@Override
	public Item getDoorItem()
	{
		return null;
	}

	@Override
	public boolean isCollidable()
	{
		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		return null;
	}

	@Override
	public int getRenderType()
	{
		return 8;
	}
	
}