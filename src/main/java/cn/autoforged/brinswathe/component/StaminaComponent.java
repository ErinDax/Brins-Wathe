package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.BrinsWathe;
import cn.autoforged.brinswathe.CowboyDuel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class StaminaComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<StaminaComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "stamina"),
        StaminaComponent.class
    );

    private static final int DEFAULT_MAX_STAMINA = 150;
    private static final float DEFAULT_RUN_SPEED = 0.1f;
    private static final int DEFAULT_REGEN_RATE = 2;
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "run_speed");
    private static final AttributeModifier.Operation SPEED_OP = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    private static final Gson OVERRIDE_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String OVERRIDE_FILE_NAME = "brinswathe-stamina.json";
    private static volatile Integer globalMaxStaminaOverride;
    private static volatile Float globalRunSpeedOverride;
    private static volatile Integer globalRegenRateOverride;
    private static volatile long globalSettingsRevision;

    private final Player player;
    public int maxStamina = DEFAULT_MAX_STAMINA;
    public int currentStamina = DEFAULT_MAX_STAMINA;
    public float runSpeed = DEFAULT_RUN_SPEED;
    public int regenRate = DEFAULT_REGEN_RATE;
    private float regenProgress;
    private boolean exhausted;
    private long appliedGlobalSettingsRevision = -1L;

    public StaminaComponent(Player player) {
        this.player = player;
        applyGlobalOverrides(false);
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; nothing may reset or tick down.
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer)) return;
        applyGlobalOverrides();
        if (usesRunSpeedBonus(this.player)) {
            applySpeedModifier();
        } else {
            removeSpeedModifier();
        }
    }

    public static void setGlobalMaxStamina(int value) {
        globalMaxStaminaOverride = Math.max(1, value);
        globalSettingsRevision++;
        persistOverrides();
    }

    public static void setGlobalRunSpeed(float value) {
        globalRunSpeedOverride = Math.max(0.0F, value);
        globalSettingsRevision++;
        persistOverrides();
    }

    public static void setGlobalRegenRate(int value) {
        globalRegenRateOverride = Math.max(0, value);
        globalSettingsRevision++;
        persistOverrides();
    }

    public static void clearGlobalOverrides() {
        globalMaxStaminaOverride = null;
        globalRunSpeedOverride = null;
        globalRegenRateOverride = null;
        globalSettingsRevision++;
        persistOverrides();
    }

    /** Restores the last `/setbrinspeed` values so a join or restart does not require typing them again. */
    public static void loadPersistedOverrides() {
        Path path = overridePath();
        if (!Files.exists(path)) return;
        try {
            JsonObject json = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            globalMaxStaminaOverride = json.has("max_stamina") ? json.get("max_stamina").getAsInt() : null;
            globalRunSpeedOverride = json.has("run_speed") ? json.get("run_speed").getAsFloat() : null;
            globalRegenRateOverride = json.has("regen_rate") ? json.get("regen_rate").getAsInt() : null;
            globalSettingsRevision++;
        } catch (Exception exception) {
            BrinsWathe.LOGGER.error("Failed to load {}: {}", OVERRIDE_FILE_NAME, exception.getMessage());
        }
    }

    /**
     * Sprint bonus is no longer tied to the civilian stamina bar: every living survival player in a
     * running round gets the same `/setbrinspeed runSpeed` multiplier, killers included.
     */
    public static boolean usesRunSpeedBonus(Player player) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        return gameWorld != null
            && gameWorld.isRunning()
            && GameFunctions.isPlayerAliveAndSurvival(player);
    }

    public static boolean usesGameStamina(Player player) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (gameWorld == null
            || !gameWorld.isRunning()
            || !GameFunctions.isPlayerAliveAndSurvival(player)
            || gameWorld.canUseKillerFeatures(player)) {
            return false;
        }

        Role role = gameWorld.getRole(player);
        return role != null && role.getMaxSprintTime() >= 0;
    }

    public void applyGlobalOverrides() {
        applyGlobalOverrides(true);
    }

    /** Join path: stamp the persisted values onto this player and push them to the client immediately. */
    public void applyGlobalOverridesForced() {
        this.appliedGlobalSettingsRevision = -1L;
        applyGlobalOverrides(true);
    }

    private void applyGlobalOverrides(boolean sync) {
        long revision = globalSettingsRevision;
        if (this.appliedGlobalSettingsRevision == revision) return;

        if (globalMaxStaminaOverride != null) {
            this.maxStamina = globalMaxStaminaOverride;
            if (sync) {
                this.currentStamina = this.maxStamina;
                this.regenProgress = 0.0F;
            } else {
                this.currentStamina = Math.min(this.currentStamina, this.maxStamina);
            }
        }
        if (globalRunSpeedOverride != null) this.runSpeed = globalRunSpeedOverride;
        if (globalRegenRateOverride != null) this.regenRate = globalRegenRateOverride;
        this.appliedGlobalSettingsRevision = revision;
        if (sync && this.player instanceof ServerPlayer) this.sync();
    }

    public void resetToInitialSettings() {
        this.maxStamina = DEFAULT_MAX_STAMINA;
        this.currentStamina = DEFAULT_MAX_STAMINA;
        this.runSpeed = DEFAULT_RUN_SPEED;
        this.regenRate = DEFAULT_REGEN_RATE;
        this.regenProgress = 0.0F;
        this.exhausted = false;
        this.appliedGlobalSettingsRevision = globalSettingsRevision;
        if (usesRunSpeedBonus(this.player)) {
            applySpeedModifier();
        } else {
            removeSpeedModifier();
        }
        this.sync();
    }

    public void tickGameStamina(boolean sprinting) {
        int previousStamina = this.currentStamina;
        boolean previousExhausted = this.exhausted;
        // A held sprint key used to reach this method as "sprinting" every tick even at zero stamina,
        // resetting the regen progress forever. Draining now requires stamina to spend; an empty tank
        // falls through to regeneration no matter what the sprint flag claims.
        if (sprinting && this.currentStamina > 0) {
            this.currentStamina--;
            this.regenProgress = 0.0F;
            if (this.currentStamina == 0) this.exhausted = true;
        } else if (this.currentStamina < this.maxStamina && this.regenRate > 0) {
            this.regenProgress += this.regenRate / 20.0F;
            int recovered = (int) this.regenProgress;
            if (recovered > 0) {
                this.currentStamina = Math.min(this.maxStamina, this.currentStamina + recovered);
                this.regenProgress -= recovered;
            }
        } else {
            this.regenProgress = 0.0F;
        }

        // Exhaustion holds sprinting shut until a fifth of the bar is back, otherwise the player would
        // flicker between one point of stamina and zero as long as the key stays held.
        if (this.exhausted && this.currentStamina >= this.exhaustionRecoveryThreshold()) {
            this.exhausted = false;
        }

        if (this.currentStamina != previousStamina || this.exhausted != previousExhausted) this.sync();
    }

    public boolean canSprint() {
        return this.currentStamina > 0 && !this.exhausted;
    }

    private int exhaustionRecoveryThreshold() {
        return Math.max(1, this.maxStamina / 5);
    }

    public void setMaxStamina(int value) {
        this.maxStamina = Math.max(1, value);
        this.currentStamina = this.maxStamina;
        this.regenProgress = 0.0F;
        this.sync();
    }

    public void reset() {
        applyGlobalOverrides();
        this.currentStamina = this.maxStamina;
        this.regenProgress = 0.0F;
        this.exhausted = false;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    private void applySpeedModifier() {
        AttributeInstance attr = this.player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) return;

        AttributeModifier existing = attr.getModifier(SPEED_MODIFIER_ID);
        double targetSpeed = this.runSpeed;

        if (existing != null && Math.abs(existing.amount() - targetSpeed) < 0.001) {
            return;
        }

        if (existing != null) {
            attr.removeModifier(SPEED_MODIFIER_ID);
        }

        attr.addPermanentModifier(new AttributeModifier(SPEED_MODIFIER_ID, targetSpeed, SPEED_OP));
    }

    private void removeSpeedModifier() {
        AttributeInstance attr = this.player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) attr.removeModifier(SPEED_MODIFIER_ID);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.maxStamina = tag.getInt("maxStamina");
        this.currentStamina = tag.getInt("currentStamina");
        this.runSpeed = tag.getFloat("runSpeed");
        this.regenRate = tag.getInt("regenRate");
        this.regenProgress = tag.getFloat("regenProgress");
        this.exhausted = tag.getBoolean("exhausted");
        this.appliedGlobalSettingsRevision = -1L;
        applyGlobalOverrides(false);
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("maxStamina", this.maxStamina);
        tag.putInt("currentStamina", this.currentStamina);
        tag.putFloat("runSpeed", this.runSpeed);
        tag.putInt("regenRate", this.regenRate);
        tag.putFloat("regenProgress", this.regenProgress);
        tag.putBoolean("exhausted", this.exhausted);
    }

    private static Path overridePath() {
        return FabricLoader.getInstance().getConfigDir().resolve(OVERRIDE_FILE_NAME);
    }

    private static void persistOverrides() {
        Path path = overridePath();
        JsonObject json = new JsonObject();
        if (globalMaxStaminaOverride != null) json.addProperty("max_stamina", globalMaxStaminaOverride);
        if (globalRunSpeedOverride != null) json.addProperty("run_speed", globalRunSpeedOverride);
        if (globalRegenRateOverride != null) json.addProperty("regen_rate", globalRegenRateOverride);
        try {
            if (json.size() == 0) {
                Files.deleteIfExists(path);
                return;
            }
            Files.createDirectories(path.getParent());
            Files.writeString(path, OVERRIDE_GSON.toJson(json) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            BrinsWathe.LOGGER.error("Failed to save {}: {}", OVERRIDE_FILE_NAME, exception.getMessage());
        }
    }
}
