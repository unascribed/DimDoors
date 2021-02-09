package org.dimdev.dimdoors.item;

import java.io.IOException;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.dimdoors.DimensionalDoorsInitializer;
import org.dimdev.dimdoors.block.entity.RiftBlockEntity;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import net.fabricmc.api.Environment;
import org.dimdev.dimdoors.network.s2c.PlayerInventorySlotUpdateS2CPacket;
import org.dimdev.dimdoors.rift.targets.IdMarker;
import org.dimdev.dimdoors.util.EntityUtils;
import org.dimdev.dimdoors.world.level.Counter;

import static net.fabricmc.api.EnvType.CLIENT;

public class RiftConfigurationToolItem extends Item {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final String ID = "rift_configuration_tool";

	RiftConfigurationToolItem() {
		super(new Item.Settings().group(ModItems.DIMENSIONAL_DOORS).maxCount(1).maxDamage(16));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		HitResult hit = player.raycast(RaycastHelper.REACH_DISTANCE, 0, false);

		if (world.isClient) {
			if (!RaycastHelper.hitsRift(hit, world)) {
				EntityUtils.chat(player, new TranslatableText("tools.rift_miss"));
				RiftBlockEntity.showRiftCoreUntil = System.currentTimeMillis() + DimensionalDoorsInitializer.CONFIG.getGraphicsConfig().highlightRiftCoreFor;
			}
			return new TypedActionResult<>(ActionResult.FAIL, stack);
		} else {
			Counter counter = Counter.get(stack);

			if (RaycastHelper.hitsRift(hit, world)) {
				RiftBlockEntity rift = (RiftBlockEntity) world.getBlockEntity(((BlockHitResult) hit).getBlockPos());

				if (rift.getDestination() instanceof IdMarker) {
					EntityUtils.chat(player, Text.of("Id: " + ((IdMarker) rift.getDestination()).getId()));
				} else {
					int id = counter.increment();
					sync(stack, player, hand);
					EntityUtils.chat(player, Text.of("Rift stripped of data and set to target id: " + id));

					rift.setDestination(new IdMarker(id));
				}

				return new TypedActionResult<>(ActionResult.SUCCESS, stack);
			} else {
				if(player.isSneaking()) {
					counter.set(-1);
					sync(stack, player, hand);
					EntityUtils.chat(player, Text.of("Counter has been reset."));
				} else {
					EntityUtils.chat(player, Text.of("Current Count: " + counter.count()));
				}
			}
		}

		return new TypedActionResult<>(ActionResult.SUCCESS, stack);
	}

	@Override
	@Environment(CLIENT)
	public void appendTooltip(ItemStack itemStack, World world, List<Text> list, TooltipContext tooltipContext) {
		if (I18n.hasTranslation(this.getTranslationKey() + ".info")) {
			list.add(new TranslatableText(this.getTranslationKey() + ".info"));
		}
	}

	@Override
	public ItemStack getDefaultStack() {
		ItemStack defaultStack = super.getDefaultStack();
		Counter.get(defaultStack).set(-1);
		return defaultStack;
	}

	private void sync(ItemStack stack, PlayerEntity player, Hand hand) {
		ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
		PlayerInventorySlotUpdateS2CPacket packet;
		if (hand == Hand.OFF_HAND) {
			packet = new PlayerInventorySlotUpdateS2CPacket(45, stack);
		} else {
			packet = new PlayerInventorySlotUpdateS2CPacket(serverPlayer.inventory.selectedSlot, stack);
		}
		PacketByteBuf buf = PacketByteBufs.create();
		try {
			packet.write(buf);
			ServerPlayNetworking.send(serverPlayer, PlayerInventorySlotUpdateS2CPacket.ID, buf);
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}
}
