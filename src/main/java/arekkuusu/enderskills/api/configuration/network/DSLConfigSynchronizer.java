package arekkuusu.enderskills.api.configuration.network;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLConfig;
import arekkuusu.enderskills.api.configuration.parser.DSLParser;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.skill.ModConfigurations;
import net.minecraft.nbt.NBTTagCompound;

public class DSLConfigSynchronizer extends ConfigSynchronizer {

    public final DSL localDSL = new DSL(new String[]{});
    public final DSL remoteDSL;

    public DSLConfigSynchronizer(DSL remoteDSL, String name) {
        this.remoteDSL = remoteDSL;
        ModConfigurations.setRegistry(this, name);
    }

    @Override
    public void initSyncConfig() {
        this.localDSL.dsl = this.remoteDSL.dsl;
        this.update();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", this.remoteDSL.dsl);
        this.initSyncConfig();
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        this.localDSL.dsl = NBTHelper.getArray(compound, "config");
        this.update();
    }

    @Override
    public void update() {
        DSLConfig config = DSLParser.parse(this.localDSL.dsl);
        EnderSkillsAPI.SKILL_DSL_CONFIG_MAP.put(this.getRegistryName(), config);
    }
}
