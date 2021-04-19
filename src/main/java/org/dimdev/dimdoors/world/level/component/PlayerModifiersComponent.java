package org.dimdev.dimdoors.world.level.component;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import org.dimdev.dimdoors.DimensionalDoorsComponents;
import org.dimdev.dimdoors.DimensionalDoorsInitializer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class PlayerModifiersComponent implements ComponentV3, AutoSyncedComponent {
	private int fray = 0;

	public PlayerModifiersComponent(@SuppressWarnings("unused") PlayerEntity player) {
	}

	@Override
	public void readFromNbt(NbtCompound nbt) {
		fray = nbt.getInt("Fray");
	}

	@Override
	public void writeToNbt(NbtCompound nbt) {
		nbt.putInt("Fray", fray);
	}

	@Override
	public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
		buf.writeVarInt(fray);
	}

	@Override
	public void applySyncPacket(PacketByteBuf buf) {
		fray = buf.readVarInt();
	}

	public int getFray() {
		return fray;
	}

	public int incrementFray(int amount) {
		return (fray = MathHelper.clamp(fray - amount, 0, DimensionalDoorsInitializer.getConfig().getPlayerConfig().fray.maxFray));
	}

	public static PlayerModifiersComponent get(PlayerEntity player) {
		return DimensionalDoorsComponents.PLAYER_MODIFIERS_COMPONENT_KEY.get(player);
	}

	public static int incrementFray(PlayerEntity player, int amount) {
		int v = get(player).incrementFray(amount);
		PlayerModifiersComponent.sync(player);
		return v;
	}

	public static int getFray(PlayerEntity player) {
		return get(player).getFray();
	}

	public static void sync(PlayerEntity player) {
		DimensionalDoorsComponents.PLAYER_MODIFIERS_COMPONENT_KEY.sync(player);
	}
}
