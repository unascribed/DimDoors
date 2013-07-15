package StevenDimDoors.mod_pocketDim;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import StevenDimDoors.mod_pocketDim.helpers.dimHelper;
import StevenDimDoors.mod_pocketDim.items.ItemRiftBlade;
import StevenDimDoors.mod_pocketDim.world.LimboGenerator;
import StevenDimDoors.mod_pocketDim.world.LimboProvider;
import StevenDimDoors.mod_pocketDim.world.PocketGenerator;
import StevenDimDoors.mod_pocketDim.world.pocketProvider;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class EventHookContainer
{
	private static Random rand = new Random();
	private static DDProperties properties = null;
	
	public EventHookContainer()
	{
		if (properties == null)
			properties = DDProperties.instance();
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onSoundLoad(SoundLoadEvent event) 
	{
		File dataDir = Minecraft.getMinecraft().mcDataDir;

		event.manager.soundPoolSounds.addSound("mods/DimDoors/sfx/monk.ogg", (mod_pocketDim.class.getResource("/mods/DimDoors/sfx/monk.ogg")));
		event.manager.soundPoolSounds.addSound("mods/DimDoors/sfx/crack.ogg", (mod_pocketDim.class.getResource("/mods/DimDoors/sfx/crack.ogg")));
		event.manager.soundPoolSounds.addSound("mods/DimDoors/sfx/tearing.ogg", (mod_pocketDim.class.getResource("/mods/DimDoors/sfx/tearing.ogg")));
	}

    @ForgeSubscribe
    public void onWorldLoad(WorldEvent.Load event)
    {
    	if(!mod_pocketDim.hasInitDims&&event.world.provider.dimensionId==0&&!event.world.isRemote)
    	{
    		System.out.println("Registering Pocket Dims");
    		mod_pocketDim.hasInitDims=true;
    		dimHelper.instance.unregsisterDims();
        	dimHelper.dimList.clear();
        	dimHelper.instance.interDimLinkList.clear();
        	dimHelper.instance.initPockets();
    	}
    	for(Integer ids : dimHelper.getIDs())
    	{
    		World world = dimHelper.getWorld(ids);
    		int linkCount=0;
    		
    		if(dimHelper.dimList.containsKey(world.provider.dimensionId))
    		{
    			//TODO added temporary Try/catch block to prevent a crash here, getLinksInDim needs to be looked at
    			try
    			{    			
	    			for(LinkData link:dimHelper.dimList.get(world.provider.dimensionId).getLinksInDim())
	    			{
	    				if(linkCount>100)
	    				{
	    					break;
	    				}
	    				linkCount++;
	    				int blocktoReplace = world.getBlockId(link.locXCoord, link.locYCoord, link.locZCoord);
	    				if(!mod_pocketDim.blocksImmuneToRift.contains(blocktoReplace))
	    				{
	        				dimHelper.getWorld(link.locDimID).setBlock(link.locXCoord, link.locYCoord, link.locZCoord, properties.RiftBlockID);
	    				}
	    			}
    			}
    			catch(Exception e)
    			{
    				e.printStackTrace();
    			}
    		}
    	}   
    }
    

  
    @ForgeSubscribe
	public void EntityJoinWorldEvent(net.minecraftforge.event.entity.EntityJoinWorldEvent event)
    {
    	if(event.entity instanceof EntityPlayer)
    	{
    	//	System.out.println(event.entity.worldObj.provider.dimensionId);
    	//	PacketDispatcher.sendPacketToPlayer(DimUpdatePacket.sendPacket(event.world.provider.dimensionId,1),(Player) event.entity);
    	}
    }
    @ForgeSubscribe
    public void onPlayerFall(LivingFallEvent event)
    {
    	event.setCanceled(event.entity.worldObj.provider.dimensionId==properties.LimboDimensionID);
    }
    
    @ForgeSubscribe
    public void onPlayerInteract(PlayerInteractEvent event)
    {
    	if(event.entityPlayer.worldObj.provider.dimensionId==properties.LimboDimensionID&&event.action==PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
    	{
    		int x = event.x;
    		int y = event.y;	
    		int z = event.z;
    		
    		if(event.entityPlayer.getHeldItem()!=null)
    		{
    			if(event.entityPlayer.getHeldItem().getItem() instanceof ItemBlock)
    			{
    				if(event.entityPlayer instanceof EntityPlayerMP)
    				{
    					Point3D point = new Point3D(x,y,z);
    					dimHelper.blocksToDecay.add(point);
    				}
    			}        
    		}   		
    	}        
    }

   
    @ForgeSubscribe
    public void onPlayerDrops(PlayerDropsEvent event)
    {
    	mod_pocketDim.limboSpawnInventory.put(event.entityPlayer.username, event.drops);
    }

    @ForgeSubscribe
    public void onWorldunload(WorldEvent.Unload event)
    {
     
    	
    }

    @ForgeSubscribe
    public void onWorldsave(WorldEvent.Save event)
    {
    
    	if(mod_pocketDim.hasInitDims&&event.world.provider.dimensionId==0)
    	{
    		dimHelper.instance.save();
    	}
    }
}