package xreliquary.client;

import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import xreliquary.Reliquary;
import xreliquary.blocks.tile.TileEntityMortar;
import xreliquary.client.render.ItemRendererApothecaryMortar;
import xreliquary.client.render.ItemRendererHandgun;
import xreliquary.client.render.RenderApothecaryCauldron;
import xreliquary.client.render.RenderApothecaryMortar;
import xreliquary.client.render.RenderShot;
import xreliquary.client.render.RenderThrown;
import xreliquary.client.render.RenderThrownPotion;
import xreliquary.common.CommonProxy;
import xreliquary.entities.EntityEnderStaffProjectile;
import xreliquary.entities.EntityGlowingWater;
import xreliquary.entities.EntityHolyHandGrenade;
import xreliquary.entities.EntityKrakenSlime;
import xreliquary.entities.EntitySpecialSnowball;
import xreliquary.entities.potion.EntityAttractionPotion;
import xreliquary.entities.potion.EntityFertilePotion;
import xreliquary.entities.potion.EntityThrownXRPotion;
import xreliquary.entities.shot.EntityBlazeShot;
import xreliquary.entities.shot.EntityBusterShot;
import xreliquary.entities.shot.EntityConcussiveShot;
import xreliquary.entities.shot.EntityEnderShot;
import xreliquary.entities.shot.EntityExorcismShot;
import xreliquary.entities.shot.EntityNeutralShot;
import xreliquary.entities.shot.EntitySandShot;
import xreliquary.entities.shot.EntitySeekerShot;
import xreliquary.entities.shot.EntityStormShot;
import xreliquary.event.ClientEventHandler;
import xreliquary.lib.Names;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
    }

    @Override
    public void init() {
        super.init();
        FMLCommonHandler.instance()
            .bus()
            .register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());

        this.registerRenderers();
    }

    public void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityBlazeShot.class, new RenderShot());
        RenderingRegistry.registerEntityRenderingHandler(EntityBusterShot.class, new RenderShot());
        RenderingRegistry.registerEntityRenderingHandler(EntityConcussiveShot.class, new RenderShot());
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderShot.class, new RenderShot());
        RenderingRegistry.registerEntityRenderingHandler(EntityExorcismShot.class, new RenderShot());
        RenderingRegistry.registerEntityRenderingHandler(EntityNeutralShot.class, new RenderShot());
        RenderingRegistry.registerEntityRenderingHandler(EntitySeekerShot.class, new RenderShot());
        RenderingRegistry.registerEntityRenderingHandler(EntitySandShot.class, new RenderShot());
        RenderingRegistry.registerEntityRenderingHandler(EntityStormShot.class, new RenderShot());
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowingWater.class, new RenderThrown(0));
        RenderingRegistry.registerEntityRenderingHandler(EntityAttractionPotion.class, new RenderThrown(2));
        RenderingRegistry.registerEntityRenderingHandler(EntityFertilePotion.class, new RenderThrown(11));
        RenderingRegistry.registerEntityRenderingHandler(EntityHolyHandGrenade.class, new RenderThrown(12));
        RenderingRegistry.registerEntityRenderingHandler(EntityKrakenSlime.class, new RenderThrown(13));
        RenderingRegistry
            .registerEntityRenderingHandler(EntitySpecialSnowball.class, new RenderSnowball(Items.snowball));
        RenderingRegistry
            .registerEntityRenderingHandler(EntityEnderStaffProjectile.class, new RenderSnowball(Items.ender_pearl));
        RenderingRegistry.registerEntityRenderingHandler(EntityThrownXRPotion.class, new RenderThrownPotion());

        RenderingRegistry.registerBlockHandler(new RenderApothecaryCauldron());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMortar.class, new RenderApothecaryMortar());

        MinecraftForgeClient.registerItemRenderer(
            ItemBlock.getItemFromBlock(Reliquary.CONTENT.getBlock(Names.apothecary_mortar)),
            new ItemRendererApothecaryMortar());
        MinecraftForgeClient.registerItemRenderer(Reliquary.CONTENT.getItem(Names.handgun), new ItemRendererHandgun());
    }

}
