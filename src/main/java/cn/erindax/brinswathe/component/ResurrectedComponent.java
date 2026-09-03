package cn.erindax.brinswathe.component;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.events.ModifierRemoved;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class ResurrectedComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<ResurrectedComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "resurrected"),
        ResurrectedComponent.class
    );

    private static final int MAX_LIFE_TICKS = 600;
    private static final ResourceLocation VOODOO_DEATH =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "voodoo");

    private final Player player;
    public boolean isResurrected;
    public boolean noDropOnDeath;
    public UUID resurrectedBy;
    public int lifeTicks;

    public ResurrectedComponent(Player player) {
        this.player = player;
    }

    public static boolean isResurrected(Player player) {
        ResurrectedComponent component = KEY.get(player);
        return component != null && component.isResurrected;
    }

    public boolean resurrect(UUID casterUuid) {
        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        if (game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return false;
        if (!this.player.isSpectator()) return false;
        if (isNecromancer(game, this.player)) return false;

        WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(this.player.level());
        if (modifiers != null) {
            for (Modifier modifier : modifiers.getModifiers(this.player)) {
                ModifierRemoved.EVENT.invoker().removeModifier(this.player, modifier);
            }
            modifiers.getModifiers(this.player).clear();
        }
        this.player.displayClientMessage(Component.literal("你失去了所有修饰符"), true);

        if (!game.canUseKillerFeatures(this.player)) {
            this.player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 150, 0, false, false, false));
            game.addRole(this.player, WatheRoles.KILLER);
            giveKnife(this.player);
        }

        this.isResurrected = true;
        this.resurrectedBy = casterUuid;
        this.lifeTicks = MAX_LIFE_TICKS;
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(this.player);
        if (shop != null) shop.reset();
        performResurrection();
        this.player.getCooldowns().addCooldown(WatheItems.PSYCHO_MODE, MAX_LIFE_TICKS);
        this.sync();
        return true;
    }

    public void reset() {
        this.isResurrected = false;
        this.resurrectedBy = null;
        this.lifeTicks = 0;
        this.noDropOnDeath = false;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void serverTick() {
        if (!this.isResurrected) return;
        if (this.lifeTicks > 0) {
            this.lifeTicks--;
            if (this.lifeTicks <= 100 && this.lifeTicks % 20 == 0) {
                this.player.displayClientMessage(
                    Component.literal("§c§l" + (this.lifeTicks / 20) + " 秒后死亡！"),
                    true
                );
            }
            this.sync();
            return;
        }
        forceDeath();
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.isResurrected = tag.getBoolean("isResurrected");
        this.lifeTicks = tag.getInt("lifeTicks");
        this.noDropOnDeath = tag.getBoolean("noDropOnDeath");
        this.resurrectedBy = tag.hasUUID("resurrectedBy") ? tag.getUUID("resurrectedBy") : null;
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putBoolean("isResurrected", this.isResurrected);
        tag.putInt("lifeTicks", this.lifeTicks);
        tag.putBoolean("noDropOnDeath", this.noDropOnDeath);
        if (this.resurrectedBy != null) tag.putUUID("resurrectedBy", this.resurrectedBy);
    }

    private void performResurrection() {
        this.player.displayClientMessage(Component.literal("§c§l你被复活了！但将在 30 秒后再次死亡..."), true);
        if (this.player instanceof ServerPlayer serverPlayer && this.player.isSpectator()) {
            serverPlayer.setGameMode(GameType.ADVENTURE);
        }
        if (this.resurrectedBy != null && this.player.getServer() != null) {
            ServerPlayer caster = this.player.getServer().getPlayerList().getPlayer(this.resurrectedBy);
            if (caster != null && this.player instanceof ServerPlayer revived) {
                revived.connection.teleport(caster.getX(), caster.getY(), caster.getZ(), revived.getYRot(), revived.getXRot());
                caster.displayClientMessage(
                    Component.literal("§a成功复活 " + this.player.getName().getString() + "，30秒后其将再次死亡"),
                    true
                );
            }
        }
    }

    private void forceDeath() {
        this.isResurrected = false;
        for (int i = 0; i < 5; i++) {
            GameFunctions.killPlayer(this.player, true, null, VOODOO_DEATH);
        }
        this.player.displayClientMessage(Component.literal("§4§l时限已到，你再次死亡了..."), true);
        this.sync();
    }

    private static void giveKnife(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (hasKnife(serverPlayer)) {
            serverPlayer.displayClientMessage(Component.literal("§7背包中已有匕首"), true);
            return;
        }
        serverPlayer.addItem(WatheItems.KNIFE.getDefaultInstance());
        serverPlayer.displayClientMessage(Component.literal("§a获得匕首").withStyle(ChatFormatting.GREEN), true);
    }

    private static boolean hasKnife(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(WatheItems.KNIFE)) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.is(WatheItems.KNIFE)) return true;
        }
        return player.getInventory().offhand.getFirst().is(WatheItems.KNIFE);
    }

    private static boolean isNecromancer(GameWorldComponent game, Player player) {
        var role = game.getRole(player);
        return role != null
            && "stupid_express".equals(role.identifier().getNamespace())
            && "necromancer".equals(role.identifier().getPath());
    }
}
