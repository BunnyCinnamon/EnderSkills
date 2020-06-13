package arekkuusu.enderskills.api.capability.data;

public interface IInfoCooldown {

    String COOL_DOWN = "cooldown";

    void setCooldown(int cooldown);

    int getCooldown();

    boolean hasCooldown();
}
