package gtPlusPlus.core.block.machine;

import static gregtech.api.enums.Mods.GTPlusPlus;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gtPlusPlus.GTplusplus;
import gtPlusPlus.api.interfaces.ITileTooltip;
import gtPlusPlus.core.creative.AddToCreativeTab;
import gtPlusPlus.core.item.base.itemblock.ItemBlockBasicTile;
import gtPlusPlus.core.tileentities.general.TileEntityFishTrap;
import gtPlusPlus.core.util.minecraft.InventoryUtils;

public class BlockFishTrap extends BlockContainer implements ITileTooltip {

    /**
     * Determines which tooltip is displayed within the itemblock.
     */
    private final int mTooltipID = 1;

    @Override
    public int getTooltipID() {
        return this.mTooltipID;
    }

    public BlockFishTrap() {
        super(Material.iron);
        this.setBlockName("blockFishTrap");
        this.setHardness(5f);
        this.setResistance(1f);
        this.setCreativeTab(AddToCreativeTab.tabMachines);
        GameRegistry.registerBlock(this, ItemBlockBasicTile.class, "blockFishTrap");
    }

    /**
     * Gets the block's texture. Args: side, meta
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(final int ordinalSide, final int meta) {
        return this.blockIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(final IIconRegister reg) {
        this.blockIcon = reg.registerIcon(GTPlusPlus.ID + ":TileEntities/fishtrap");
        reg.registerIcon(GTPlusPlus.ID + ":TileEntities/fishtrap");
        reg.registerIcon(GTPlusPlus.ID + ":TileEntities/fishtrap");
        reg.registerIcon(GTPlusPlus.ID + ":TileEntities/fishtrap");
    }

    /**
     * Called upon block activation (right-click on the block.)
     */
    @Override
    public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player,
        final int side, final float lx, final float ly, final float lz) {
        if (world.isRemote) {
            return true;
        }

        final TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityFishTrap) {
            player.openGui(GTplusplus.instance, 5, world, x, y, z);
            return true;
        }
        return false;
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(final World world, final int meta) {
        return new TileEntityFishTrap();
    }

    @Override
    public void onBlockAdded(final World world, final int x, final int y, final int z) {
        super.onBlockAdded(world, x, y, z);
    }

    @Override
    public void breakBlock(final World world, final int x, final int y, final int z, final Block block,
        final int number) {
        InventoryUtils.dropInventoryItems(world, x, y, z, block);
        super.breakBlock(world, x, y, z, block, number);
    }

    @Override
    public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase entity,
        final ItemStack stack) {
        if (stack.hasDisplayName()) {
            ((TileEntityFishTrap) world.getTileEntity(x, y, z)).setCustomName(stack.getDisplayName());
        }
    }

    @Override
    public boolean canCreatureSpawn(final EnumCreatureType type, final IBlockAccess world, final int x, final int y,
        final int z) {
        return false;
    }
}
