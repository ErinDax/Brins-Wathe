package cn.erindax.brinswathe;

import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import net.minecraft.world.entity.player.Player;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;
import org.BsXinQin.kinswathe.roles.physician.PhysicianComponent;
import org.aussiebox.starexpress.cca.AllergicComponent;

public final class BrinShieldPierce {
    private final Player victim;
    private final DreamerComponent dreamer;
    private final int dreamArmor;
    private final PhysicianComponent physician;
    private final int physicianArmor;
    private final AllergicComponent allergic;
    private final int allergicArmor;

    private BrinShieldPierce(Player victim) {
        this.victim = victim;
        this.dreamer = DreamerComponent.KEY.get(victim);
        this.dreamArmor = this.dreamer == null ? 0 : this.dreamer.dreamArmor;
        this.physician = PhysicianComponent.KEY.get(victim);
        this.physicianArmor = this.physician == null ? 0 : this.physician.physicianArmor;
        this.allergic = AllergicComponent.KEY.get(victim);
        this.allergicArmor = this.allergic == null ? 0 : this.allergic.armor;
    }

    public static BrinShieldPierce strip(Player victim) {
        BrinShieldPierce pierce = new BrinShieldPierce(victim);
        if (pierce.dreamArmor > 0) pierce.dreamer.dreamArmor = 0;
        if (pierce.physicianArmor > 0) pierce.physician.physicianArmor = 0;
        if (pierce.allergicArmor > 0) pierce.allergic.armor = 0;
        return pierce;
    }

    public void restore() {
        if (this.dreamArmor > 0) this.dreamer.dreamArmor = this.dreamArmor;
        if (this.physicianArmor > 0) this.physician.physicianArmor = this.physicianArmor;
        if (this.allergicArmor > 0) this.allergic.armor = this.allergicArmor;
    }

    public void commit() {
        if (this.dreamArmor > 0) this.dreamer.sync();
        if (this.physicianArmor > 0) this.physician.sync();
        if (this.allergicArmor > 0) this.allergic.sync();
        PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(this.victim);
        if (psycho != null && psycho.getArmour() > 0) psycho.setArmour(0);
    }
}
