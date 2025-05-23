package gregtech.api.gui.modularui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.builder.UIBuilder;
import com.gtnewhorizons.modularui.common.builder.UIInfo;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.GTValues;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IHasWorldObjectAndCoords;
import gregtech.api.net.GTPacketSendCoverData;
import gregtech.common.covers.Cover;

public class GTUIInfos {

    public static void init() {}

    /**
     * Generator for {@link UIInfo} which is responsible for registering and opening UIs. Unlike
     * {@link com.gtnewhorizons.modularui.api.UIInfos#TILE_MODULAR_UI}, this accepts custom constructors for UI. <br>
     * Do NOT run {@link UIBuilder#build} on-the-fly, otherwise MP client won't register UIs. Instead, store to static
     * field, just like {@link #GTTileEntityDefaultUI}. Such mistake can be easily overlooked by testing only SP.
     */
    public static final Function<ContainerConstructor, UIInfo<?, ?>> GTTileEntityUIFactory = containerConstructor -> UIBuilder
        .of()
        .container((player, world, x, y, z) -> {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof ITileWithModularUI mui) {
                return createTileEntityContainer(player, mui::createWindow, te::markDirty, containerConstructor);
            }
            return null;
        })
        .gui(((player, world, x, y, z) -> {
            if (!world.isRemote) return null;
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof ITileWithModularUI mui) {
                return createTileEntityGuiContainer(player, mui::createWindow, containerConstructor);
            }
            return null;
        }))
        .build();

    private static final UIInfo<?, ?> GTTileEntityDefaultUI = GTTileEntityUIFactory.apply(ModularUIContainer::new);

    private static final Map<ForgeDirection, UIInfo<?, ?>> coverUI = new HashMap<>();

    static {
        for (final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            coverUI.put(
                side,
                UIBuilder.of()
                    .container((player, world, x, y, z) -> {
                        final TileEntity te = world.getTileEntity(x, y, z);
                        if (!(te instanceof ICoverable gtTileEntity)) return null;
                        return gtTileEntity.getCoverAtSide(side)
                            .createCoverContainer(player);
                    })
                    .gui((player, world, x, y, z) -> {
                        if (!world.isRemote) return null;
                        final TileEntity te = world.getTileEntity(x, y, z);
                        if (!(te instanceof ICoverable gtTileEntity)) return null;
                        ModularUIContainer container = gtTileEntity.getCoverAtSide(side)
                            .createCoverContainer(player);
                        return (container == null) ? null : new ModularGui(container);
                    })
                    .build());
        }
    }

    /**
     * @deprecated Use {@link gregtech.api.metatileentity.CommonMetaTileEntity#openGui}
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void openGTTileEntityUI(IHasWorldObjectAndCoords aTileEntity, EntityPlayer aPlayer) {
        if (aTileEntity.isClientSide() || aPlayer instanceof FakePlayer) return;
        GTTileEntityDefaultUI.open(
            aPlayer,
            aTileEntity.getWorld(),
            aTileEntity.getXCoord(),
            aTileEntity.getYCoord(),
            aTileEntity.getZCoord());
    }

    /**
     * Opens cover UI, created by {@link Cover#createWindow}.
     */
    public static void openCoverUI(ICoverable tileEntity, EntityPlayer player, ForgeDirection side) {
        if (tileEntity.isClientSide()) return;

        Cover cover = tileEntity.getCoverAtSide(side);
        GTValues.NW.sendToPlayer(new GTPacketSendCoverData(cover, tileEntity, side), (EntityPlayerMP) player);

        coverUI.get(side)
            .open(
                player,
                tileEntity.getWorld(),
                tileEntity.getXCoord(),
                tileEntity.getYCoord(),
                tileEntity.getZCoord());
    }

    /**
     * Opens UI for player's item, created by
     * {@link com.gtnewhorizons.modularui.api.screen.IItemWithModularUI#createWindow}.
     */
    public static void openPlayerHeldItemUI(EntityPlayer player) {
        if (NetworkUtils.isClient()) return;
        UIInfos.PLAYER_HELD_ITEM_UI.open(player);
    }

    private static ModularUIContainer createTileEntityContainer(EntityPlayer player,
        Function<UIBuildContext, ModularWindow> windowCreator, Runnable onWidgetUpdate,
        ContainerConstructor containerCreator) {
        final UIBuildContext buildContext = new UIBuildContext(player);
        final ModularWindow window = windowCreator.apply(buildContext);
        if (window == null) return null;
        return containerCreator.of(new ModularUIContext(buildContext, onWidgetUpdate), window);
    }

    @SideOnly(Side.CLIENT)
    private static ModularGui createTileEntityGuiContainer(EntityPlayer player,
        Function<UIBuildContext, ModularWindow> windowCreator, ContainerConstructor containerConstructor) {
        final ModularUIContainer container = createTileEntityContainer(
            player,
            windowCreator,
            null,
            containerConstructor);
        return (container == null) ? null : new ModularGui(container);
    }

    @FunctionalInterface
    public interface ContainerConstructor {

        ModularUIContainer of(ModularUIContext context, ModularWindow mainWindow);
    }
}
