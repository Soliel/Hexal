package ram.talia.hexal.fabric;

import at.petrak.hexcasting.api.misc.MediaConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.api.config.HexalConfig;
import ram.talia.hexal.xplat.IXplatAbstractions;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Config(name = HexalAPI.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/calcite.png")
public class FabricHexalConfig extends PartitioningSerializer.GlobalData {
    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.TransitiveObject
    public final Common common = new Common();
    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.TransitiveObject
    public final Client client = new Client();
    @ConfigEntry.Category("server")
    @ConfigEntry.Gui.TransitiveObject
    public final Server server = new Server();

    public static FabricHexalConfig setup() {
        AutoConfig.register(FabricHexalConfig.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        var instance = AutoConfig.getConfigHolder(FabricHexalConfig.class).getConfig();

        HexalConfig.setCommon(instance.common);
        // We care about the client only on the *physical* client ...
        if (IXplatAbstractions.INSTANCE.isPhysicalClient()) {
            HexalConfig.setClient(instance.client);
        }
        // but we care about the server on the *logical* server
        // i believe this should Just Work without a guard? assuming we don't access it from the client ever
        HexalConfig.setServer(instance.server);

        return instance;
    }


    @Config(name = "common")
    private static class Common implements ConfigData, HexalConfig.CommonConfigAccess { }

    @Config(name = "client")
    private static class Client implements ConfigData, HexalConfig.ClientConfigAccess { }


    @Config(name = "server")
    private static class Server implements ConfigData, HexalConfig.ServerConfigAccess {

        private boolean generateSlipwayGeodes = DEFAULT_GENERATE_SLIPWAY_GEODES;

        @ConfigEntry.Gui.CollapsibleObject
        private MiscSpells miscSpells = new MiscSpells();

        static class MiscSpells {
            // costs of misc spells
            double fallingBlockCost = DEFAULT_FALLING_BLOCK_COST;
            double freezeCost = DEFAULT_FREEZE_COST;
            double particlesCost = DEFAULT_PARTICLES_COST;
            double placeTypeCost = DEFAULT_PLACE_TYPE_COST;
            double smeltCost = DEFAULT_SMELT_COST;
        }

        @ConfigEntry.Gui.CollapsibleObject
        private WispSpells wispSpells = new WispSpells();

        static class WispSpells {
            // costs of wisp spells
            @ConfigEntry.Gui.Tooltip(count = 2)
            double moveSpeedSetCost = DEFAULT_MOVE_SPEED_SET_COST;
            double summonTickingWispCost = DEFAULT_SUMMON_TICKING_WISP_COST;
            double summonProjectileWispCost = DEFAULT_SUMMON_PROJECTILE_WISP_COST;
            double summonProjectileWispMinCost = DEFAULT_SUMMON_PROJECTILE_WISP_MIN_COST;
        }

        @ConfigEntry.Gui.CollapsibleObject
        private WispUpkeep wispUpkeep = new WispUpkeep();

        static class WispUpkeep {
            // costs of wisp upkeep
            double tickingWispUpkeepPerTick = DEFAULT_TICKING_WISP_UPKEEP_PER_TICK;
            double projectileWispUpkeepPerTick = DEFAULT_PROJECTILE_WISP_UPKEEP_PER_TICK;
            @ConfigEntry.Gui.Tooltip(count = 2)
            double untriggeredWispUpkeepDiscount = DEFAULT_UNTRIGGERED_WISP_UPKEEP_DISCOUNT;
            double linkUpkeepPerTick = DEFAULT_LINK_UPKEEP_PER_TICK;
            @ConfigEntry.Gui.Tooltip(count = 2)
            double seonDiscountFactor = DEFAULT_SEON_DISCOUNT_FACTOR;
        }


        @ConfigEntry.Gui.CollapsibleObject
        private LinkSpells linkSpells = new LinkSpells();

        static class LinkSpells {
            // costs of link spells
            double linkCost = DEFAULT_LINK_COST;
            double sendIotaCost = DEFAULT_SEND_IOTA_COST;
            double unlinkCost = DEFAULT_UNLINK_COST;
        }


        @ConfigEntry.Gui.CollapsibleObject
        private GateSpells gateSpells = new GateSpells();

        static class GateSpells {
            // costs of gate spells
            double makeGateCost = DEFAULT_MAKE_GATE_COST;
            double markGateCost = DEFAULT_MARK_GATE_COST;
            double closeGateCost = DEFAULT_CLOSE_GATE_COST;
        }


        @ConfigEntry.Gui.CollapsibleObject
        private GreatSpells greatSpells = new GreatSpells();

        static class GreatSpells {
            // costs of great spells
            @ConfigEntry.Gui.Tooltip(count = 2)
            double consumeWispOwnCost = DEFAULT_CONSUME_WISP_OWN_COST;
            @ConfigEntry.Gui.Tooltip(count = 3)
            double consumeWispOthersCostPerMedia = DEFAULT_CONSUME_WISP_OTHERS_COST_PER_MEDIA;
            double seonWispSetCost = DEFAULT_SEON_WISP_SET_COST;
            @ConfigEntry.Gui.Tooltip
            double tickConstantCost = DEFAULT_TICK_CONSTANT_COST;
            @ConfigEntry.Gui.Tooltip(count = 2)
            double tickCostPerTicked = DEFAULT_TICK_COST_PER_TICKED;
            @ConfigEntry.BoundedDiscrete(min = MIN_TICK_RANDOM_TICK_I_PROB, max = MAX_TICK_RANDOM_TICK_I_PROB)
            @ConfigEntry.Gui.Tooltip(count = 3)
            int tickRandomTickIProb = DEFAULT_TICK_RANDOM_TICK_I_PROB;
        }



        @Override
        public void validatePostLoad() throws ValidationException {
            // costs of misc spells
            this.miscSpells.fallingBlockCost = bound(this.miscSpells.fallingBlockCost, DEF_MIN_COST, DEF_MAX_COST);
            this.miscSpells.freezeCost = bound(this.miscSpells.freezeCost, DEF_MIN_COST, DEF_MAX_COST);
            this.miscSpells.particlesCost = bound(this.miscSpells.particlesCost, DEF_MIN_COST, DEF_MAX_COST);
            this.miscSpells.placeTypeCost = bound(this.miscSpells.placeTypeCost, DEF_MIN_COST, DEF_MAX_COST);
            this.miscSpells.smeltCost = bound(this.miscSpells.smeltCost, DEF_MIN_COST, DEF_MAX_COST);

            // costs of wisp spells
            this.wispSpells.moveSpeedSetCost = bound(this.wispSpells.moveSpeedSetCost, DEF_MIN_COST, DEF_MAX_COST);
            this.wispSpells.summonTickingWispCost = bound(this.wispSpells.summonTickingWispCost, DEF_MIN_COST, DEF_MAX_COST);
            this.wispSpells.summonProjectileWispCost = bound(this.wispSpells.summonProjectileWispCost, DEF_MIN_COST, DEF_MAX_COST);
            this.wispSpells.summonProjectileWispMinCost = bound(this.wispSpells.summonProjectileWispMinCost, DEF_MIN_COST, DEF_MAX_COST);

            // costs of wisp upkeep
            this.wispUpkeep.tickingWispUpkeepPerTick = bound(this.wispUpkeep.tickingWispUpkeepPerTick, DEF_MIN_COST, DEF_MAX_COST);
            this.wispUpkeep.projectileWispUpkeepPerTick = bound(this.wispUpkeep.projectileWispUpkeepPerTick, DEF_MIN_COST, DEF_MAX_COST);
            this.wispUpkeep.untriggeredWispUpkeepDiscount = bound(this.wispUpkeep.untriggeredWispUpkeepDiscount, MIN_UNTRIGGERED_WISP_UPKEEP_DISCOUNT, MAX_UNTRIGGERED_WISP_UPKEEP_DISCOUNT);
            this.wispUpkeep.linkUpkeepPerTick = bound(this.wispUpkeep.linkUpkeepPerTick, DEF_MIN_COST, DEF_MAX_COST);
            this.wispUpkeep.seonDiscountFactor = bound(this.wispUpkeep.seonDiscountFactor, MIN_SEON_DISCOUNT_FACTOR, MAX_SEON_DISCOUNT_FACTOR);

            // costs of link spells
            this.linkSpells.linkCost = bound(this.linkSpells.linkCost, DEF_MIN_COST, DEF_MAX_COST);
            this.linkSpells.sendIotaCost = bound(this.linkSpells.sendIotaCost, DEF_MIN_COST, DEF_MAX_COST);
            this.linkSpells.unlinkCost = bound(this.linkSpells.unlinkCost, DEF_MIN_COST, DEF_MAX_COST);

            // costs of gate spells
            this.gateSpells.makeGateCost = bound(this.gateSpells.makeGateCost, DEF_MIN_COST, DEF_MAX_COST);
            this.gateSpells.markGateCost = bound(this.gateSpells.markGateCost, DEF_MIN_COST, DEF_MAX_COST);
            this.gateSpells.closeGateCost = bound(this.gateSpells.closeGateCost, DEF_MIN_COST, DEF_MAX_COST);

            // costs of great spells
            this.greatSpells.consumeWispOwnCost = bound(this.greatSpells.consumeWispOwnCost, DEF_MIN_COST, DEF_MAX_COST);
            this.greatSpells.consumeWispOthersCostPerMedia = bound(this.greatSpells.consumeWispOthersCostPerMedia, MIN_CONSUME_WISP_OTHERS_COST_PER_MEDIA, MAX_CONSUME_WISP_OTHERS_COST_PER_MEDIA);
            this.greatSpells.seonWispSetCost = bound(this.greatSpells.seonWispSetCost, DEF_MIN_COST, DEF_MAX_COST);
            this.greatSpells.tickConstantCost = bound(this.greatSpells.tickConstantCost, DEF_MIN_COST, DEF_MAX_COST);
            this.greatSpells.tickCostPerTicked = bound(this.greatSpells.tickCostPerTicked, DEF_MIN_COST, DEF_MAX_COST);

            this.greatSpells.tickRandomTickIProb = bound(this.greatSpells.tickRandomTickIProb, MIN_TICK_RANDOM_TICK_I_PROB, MAX_TICK_RANDOM_TICK_I_PROB);
        }

        private int bound(int toBind, int lower, int upper) {
            return Math.min(Math.max(toBind, lower), upper);
        }
        private double bound(double toBind, double lower, double upper) {
            return Math.min(Math.max(toBind, lower), upper);
        }


        //region getters
        @Override
        public boolean getGenerateSlipwayGeodes() {
            return generateSlipwayGeodes;
        }

        @Override
        public int getFallingBlockCost() {
            return (int) (miscSpells.fallingBlockCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getFreezeCost() {
            return (int) (miscSpells.freezeCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getParticlesCost() {
            return (int) (miscSpells.particlesCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getPlaceTypeCost() {
            return (int) (miscSpells.placeTypeCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getSmeltCost() {
            return (int) (miscSpells.smeltCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getMoveSpeedSetCost() {
            return (int) (wispSpells.moveSpeedSetCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getSummonTickingWispCost() {
            return (int) (wispSpells.summonTickingWispCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getSummonProjectileWispCost() {
            return (int) (wispSpells.summonProjectileWispCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getSummonProjectileWispMinCost() {
            return (int) (wispSpells.summonProjectileWispMinCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getTickingWispUpkeepPerTick() {
            return (int) (wispUpkeep.tickingWispUpkeepPerTick * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getProjectileWispUpkeepPerTick() {
            return (int) (wispUpkeep.projectileWispUpkeepPerTick * MediaConstants.DUST_UNIT);
        }

        @Override
        public double getUntriggeredWispUpkeepDiscount() {
            return wispUpkeep.untriggeredWispUpkeepDiscount;
        }

        @Override
        public int getLinkUpkeepPerTick() {
            return (int) (wispUpkeep.linkUpkeepPerTick * MediaConstants.DUST_UNIT);
        }

        @Override
        public double getSeonDiscountFactor() {
            return wispUpkeep.seonDiscountFactor;
        }

        @Override
        public int getLinkCost() {
            return (int) (linkSpells.linkCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getSendIotaCost() {
            return (int) (linkSpells.sendIotaCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getUnlinkCost() {
            return (int) (linkSpells.unlinkCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getMakeGateCost() {
            return (int) (gateSpells.makeGateCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getMarkGateCost() {
            return (int) (gateSpells.markGateCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getCloseGateCost() {
            return (int) (gateSpells.closeGateCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getConsumeWispOwnCost() {
            return (int) (greatSpells.consumeWispOwnCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public double getConsumeWispOthersCostPerMedia() {
            return greatSpells.consumeWispOthersCostPerMedia;
        }

        @Override
        public int getSeonWispSetCost() {
            return (int) (greatSpells.seonWispSetCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getTickConstantCost() {
            return (int) (greatSpells.tickConstantCost * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getTickCostPerTicked() {
            return (int) (greatSpells.tickCostPerTicked * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getTickRandomTickIProb() {
            return greatSpells.tickRandomTickIProb;
        }
        //endregion
    }
}
