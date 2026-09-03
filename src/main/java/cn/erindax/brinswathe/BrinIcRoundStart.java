package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.ResurrectedComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.index.WatheItems;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ModifierRemoved;
import org.agmas.harpymodloader.modifiers.Modifier;

public final class BrinIcRoundStart {
    private static final ResourceLocation MASTER_KEY_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "master_key");
    private static final ResourceLocation NECROMANCER_ID =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "necromancer");

    private BrinIcRoundStart() {
    }

    public static void apply(Level world, GameWorldComponent game) {
        applyPlanB(world, game);
        applyBadGuesser(world, game);
        for (Player player : world.players()) {
            if (!(player instanceof ServerPlayer serverPlayer)) continue;
            stampLetters(serverPlayer, game);
            stampFakeKnives(serverPlayer);
        }
    }

    public static void resetPlayer(Player player) {
        ResurrectedComponent resurrected = ResurrectedComponent.KEY.get(player);
        if (resurrected != null) resurrected.reset();
    }

    private static void applyPlanB(Level world, GameWorldComponent game) {
        if (!BrinIcFlags.planB || BrinIcModifiers.PLAN_B == null) return;
        boolean conductorAmongGood = false;
        Role conductor = BrinNoelleAccess.findRole(BrinNoelleAccess.CONDUCTOR_ID);
        if (conductor != null) {
            for (Player player : world.players()) {
                Role role = game.getRole(player);
                if (role != null && conductor.equals(role) && role.isInnocent() && !role.canUseKiller()) {
                    conductorAmongGood = true;
                    break;
                }
            }
        }
        if (conductorAmongGood) return;
        List<UUID> vigilantes = game.getAllWithRole(WatheRoles.VIGILANTE);
        if (vigilantes.isEmpty()) return;
        Player selected = world.getPlayerByUUID(vigilantes.get(world.getRandom().nextInt(vigilantes.size())));
        if (selected == null) return;
        WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(world);
        modifiers.addModifier(selected.getUUID(), BrinIcModifiers.PLAN_B);
        ModifierAssigned.EVENT.invoker().assignModifier(selected, BrinIcModifiers.PLAN_B);
        Item masterKey = BuiltInRegistries.ITEM.get(MASTER_KEY_ID);
        if (masterKey != Items.AIR) selected.addItem(new ItemStack(masterKey));
    }

    private static void applyBadGuesser(Level world, GameWorldComponent game) {
        if (!BrinIcFlags.badGuesser || BrinIcModifiers.GUESSER2 == null) return;
        Modifier guesser = BrinIcModifiers.findRegistered(BrinModifiers.GUESSER);
        if (guesser == null) return;
        WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(world);
        List<UUID> guessers = new ArrayList<>(modifiers.getAllWithModifier(guesser));
        if (guessers.isEmpty()) return;
        Role insane = BrinNoelleAccess.findRole(BrinNoelleAccess.INSANE_KILLER_ID);
        boolean hasInsane = insane != null && !game.getAllWithRole(insane).isEmpty();
        if (!hasNecromancer(game, world) && !hasInsane) {
            return;
        }
        for (UUID guesserId : guessers) {
            modifiers.getModifiers(guesserId).remove(guesser);
            Player guesserPlayer = world.getPlayerByUUID(guesserId);
            if (guesserPlayer == null) continue;
            ModifierRemoved.EVENT.invoker().removeModifier(guesserPlayer, guesser);
            modifiers.addModifier(guesserId, BrinIcModifiers.GUESSER2);
            ModifierAssigned.EVENT.invoker().assignModifier(guesserPlayer, BrinIcModifiers.GUESSER2);
            guesserPlayer.sendSystemMessage(Component.literal("你的猜测者能力已被禁用,替换为全队资金50！").withStyle(ChatFormatting.GOLD));
            for (UUID killerId : game.getAllKillerTeamPlayers()) {
                Player killer = world.getPlayerByUUID(killerId);
                if (killer == null) continue;
                PlayerShopComponent shop = PlayerShopComponent.KEY.get(killer);
                if (shop != null) shop.addToBalance(50);
            }
        }
    }

    private static boolean hasNecromancer(GameWorldComponent game, Level world) {
        for (Player player : world.players()) {
            Role role = game.getRole(player);
            if (role != null && NECROMANCER_ID.equals(role.identifier())) return true;
        }
        return false;
    }

    private static void stampLetters(ServerPlayer player, GameWorldComponent game) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(WatheItems.LETTER)) continue;
            Component roleLine = Component.translatableWithFallback(
                    "tip.letter.role",
                    "你的职业是 %s",
                    formatRole(game.getRole(player)))
                .withStyle(style -> style.withItalic(false).withColor(0xC5B08B));
            ItemLore oldLore = stack.get(DataComponents.LORE);
            stack.set(DataComponents.LORE, appendRoleLore(roleLine, oldLore));
            break;
        }
    }

    private static void stampFakeKnives(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            BrinKnifeSkins.stampOwnerAndSkin(player, player.getInventory().getItem(slot));
        }
    }

    private static Component formatRole(Role role) {
        if (role == null) return Component.literal("未知");
        ResourceLocation id = role.identifier();
        String nsKey = "announcement.role." + id.getNamespace() + "." + id.getPath();
        String plainKey = "announcement.role." + id.getPath();
        MutableComponent text = Language.getInstance().has(nsKey)
            ? Component.translatable(nsKey)
            : (Language.getInstance().has(plainKey) ? Component.translatable(plainKey) : Component.literal(id.getPath()));
        return text.withStyle(style -> style.withColor(role.color()).withItalic(false));
    }

    private static ItemLore appendRoleLore(Component roleLine, ItemLore lore) {
        List<Component> old = lore != null && lore.lines() != null ? lore.lines() : List.of();
        ArrayList<Component> lines = new ArrayList<>();
        if (!old.isEmpty()) {
            lines.add(old.getFirst());
            lines.add(roleLine);
            lines.addAll(old.subList(1, old.size()));
        } else {
            lines.add(roleLine);
        }
        return new ItemLore(lines);
    }
}
