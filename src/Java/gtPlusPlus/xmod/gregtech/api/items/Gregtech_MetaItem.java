package gtPlusPlus.xmod.gregtech.api.items;

import static gregtech.api.enums.GT_Values.D1;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Materials;
import gregtech.api.enums.SubTag;
import gregtech.api.enums.TC_Aspects.TC_AspectStack;
import gregtech.api.interfaces.IFoodStat;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.IItemContainer;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GT_LanguageManager;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Utility;
import gtPlusPlus.core.creative.AddToCreativeTab;
import gtPlusPlus.core.lib.CORE;
import gtPlusPlus.xmod.gregtech.api.interfaces.internal.Interface_ItemBehaviour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class Gregtech_MetaItem extends Gregtech_MetaItem_Base {
    /**
     * All instances of this Item Class are listed here.
     * This gets used to register the Renderer to all Items of this Type, if useStandardMetaItemRenderer() returns true.
     * <p/>
     * You can also use the unlocalized Name gotten from getUnlocalizedName() as Key if you want to get a specific Item.
     */
    public static final HashMap<String, Gregtech_MetaItem> sInstances = new HashMap<String, Gregtech_MetaItem>();

	/* ---------- CONSTRUCTOR AND MEMBER VARIABLES ---------- */

    public final short mOffset, mItemAmount;
    public final BitSet mEnabledItems;
    public final BitSet mVisibleItems;
    public final IIcon[][] mIconList;

    public final HashMap<Short, IFoodStat> mFoodStats = new HashMap<Short, IFoodStat>();
    public final HashMap<Short, Long[]> mElectricStats = new HashMap<Short, Long[]>();
    public final HashMap<Short, Long[]> mFluidContainerStats = new HashMap<Short, Long[]>();
    public final HashMap<Short, Short> mBurnValues = new HashMap<Short, Short>();

    /**
     * Creates the Item using these Parameters.
     *
     * @param aUnlocalized The Unlocalized Name of this Item.
     */
    public Gregtech_MetaItem(String aUnlocalized, short aOffset, short aItemAmount) {
        super(aUnlocalized);
        setCreativeTab(AddToCreativeTab.tabMachines);
        setHasSubtypes(true);
        setMaxDamage(0);
        mEnabledItems = new BitSet(aItemAmount);
        mVisibleItems = new BitSet(aItemAmount);

        mOffset = (short) Math.min(32766, aOffset);
        mItemAmount = (short) Math.min(aItemAmount, 32766 - mOffset);
        mIconList = new IIcon[aItemAmount][1];

        sInstances.put(getUnlocalizedName(), this);
    }

    /**
     * This adds a Custom Item to the ending Range.
     *
     * @param aID           The Id of the assigned Item [0 - mItemAmount] (The MetaData gets auto-shifted by +mOffset)
     * @param aEnglish      The Default Localized Name of the created Item
     * @param aToolTip      The Default ToolTip of the created Item, you can also insert null for having no ToolTip
     * @param aFoodBehavior The Food Value of this Item. Can be null aswell. Just a convenience thing.
     * @param aRandomData   The OreDict Names you want to give the Item. Also used for TC Aspects and some other things.
     * @return An ItemStack containing the newly created Item.
     */
    public final ItemStack addItem(int aID, String aEnglish, String aToolTip, Object... aRandomData) {
        if (aToolTip == null) aToolTip = "";
        if (aID >= 0 && aID < mItemAmount) {
            ItemStack rStack = new ItemStack(this, 1, mOffset + aID);
            mEnabledItems.set(aID);
            mVisibleItems.set(aID);
            GT_LanguageManager.addStringLocalization(getUnlocalizedName(rStack) + ".name", aEnglish);
            GT_LanguageManager.addStringLocalization(getUnlocalizedName(rStack) + ".tooltip", aToolTip);
            List<TC_AspectStack> tAspects = new ArrayList<TC_AspectStack>();
            // Important Stuff to do first
            for (Object tRandomData : aRandomData)
                if (tRandomData instanceof SubTag) {
                    if (tRandomData == SubTag.INVISIBLE) {
                        mVisibleItems.set(aID, false);
                        continue;
                    }
                    if (tRandomData == SubTag.NO_UNIFICATION) {
                        GT_OreDictUnificator.addToBlacklist(rStack);
                        continue;
                    }
                }
            // now check for the rest
            for (Object tRandomData : aRandomData)
                if (tRandomData != null) {
                    boolean tUseOreDict = true;
                   
                    if (tRandomData instanceof Interface_ItemBehaviour) {
                        addItemBehavior(mOffset + aID, (Interface_ItemBehaviour<Gregtech_MetaItem_Base>) tRandomData);
                        tUseOreDict = false;
                    }
                    if (tRandomData instanceof IItemContainer) {
                        ((IItemContainer) tRandomData).set(rStack);
                        tUseOreDict = false;
                    }
                    if (tRandomData instanceof SubTag) {
                        continue;
                    }
                    if (tRandomData instanceof TC_AspectStack) {
                        ((TC_AspectStack) tRandomData).addToAspectList(tAspects);
                        continue;
                    }
                    if (tRandomData instanceof ItemData) {
                        if (GT_Utility.isStringValid(tRandomData))
                            GT_OreDictUnificator.registerOre(tRandomData, rStack);
                        else GT_OreDictUnificator.addItemData(rStack, (ItemData) tRandomData);
                        continue;
                    }
                    if (tUseOreDict) {
                        GT_OreDictUnificator.registerOre(tRandomData, rStack);
                        continue;
                    }
                }
            if (GregTech_API.sThaumcraftCompat != null)
                GregTech_API.sThaumcraftCompat.registerThaumcraftAspectsToItem(rStack, tAspects, false);
            return rStack;
        }
        return null;
    }

    /**
     * Sets the Furnace Burn Value for the Item.
     *
     * @param aMetaValue the Meta Value of the Item you want to set it to. [0 - 32765]
     * @param aValue     200 = 1 Burn Process = 500 EU, max = 32767 (that is 81917.5 EU)
     * @return the Item itself for convenience in constructing.
     */
    public final Gregtech_MetaItem setBurnValue(int aMetaValue, int aValue) {
        if (aMetaValue < 0 || aMetaValue >= mOffset + mEnabledItems.length() || aValue < 0) return this;
        if (aValue == 0) mBurnValues.remove((short) aMetaValue);
        else mBurnValues.put((short) aMetaValue, aValue > Short.MAX_VALUE ? Short.MAX_VALUE : (short) aValue);
        return this;
    }

    /**
     * @param aMetaValue     the Meta Value of the Item you want to set it to. [0 - 32765]
     * @param aMaxCharge     Maximum Charge. (if this is == 0 it will remove the Electric Behavior)
     * @param aTransferLimit Transfer Limit.
     * @param aTier          The electric Tier.
     * @param aSpecialData   If this Item has a Fixed Charge, like a SingleUse Battery (if > 0).
     *                       Use -1 if you want to make this Battery chargeable (the use and canUse Functions will still discharge if you just use this)
     *                       Use -2 if you want to make this Battery dischargeable.
     *                       Use -3 if you want to make this Battery charge/discharge-able.
     * @return the Item itself for convenience in constructing.
     */
    public final Gregtech_MetaItem setElectricStats(int aMetaValue, long aMaxCharge, long aTransferLimit, long aTier, long aSpecialData, boolean aUseAnimations) {
        if (aMetaValue < 0 || aMetaValue >= mOffset + mEnabledItems.length()) return this;
        if (aMaxCharge == 0) mElectricStats.remove((short) aMetaValue);
        else {
            mElectricStats.put((short) aMetaValue, new Long[]{aMaxCharge, Math.max(0, aTransferLimit), Math.max(-1, aTier), aSpecialData});
            if (aMetaValue >= mOffset && aUseAnimations)
                mIconList[aMetaValue - mOffset] = Arrays.copyOf(mIconList[aMetaValue - mOffset], Math.max(9, mIconList[aMetaValue - mOffset].length));
        }
        return this;
    }

    /**
     * @param aMetaValue     the Meta Value of the Item you want to set it to. [0 - 32765]
     * @param aMaxCharge     Maximum Charge. (if this is == 0 it will remove the Electric Behavior)
     * @param aTransferLimit Transfer Limit.
     * @param aTier          The electric Tier.
     * @param aSpecialData   If this Item has a Fixed Charge, like a SingleUse Battery (if > 0).
     *                       Use -1 if you want to make this Battery chargeable (the use and canUse Functions will still discharge if you just use this)
     *                       Use -2 if you want to make this Battery dischargeable.
     *                       Use -3 if you want to make this Battery charge/discharge-able.
     * @return the Item itself for convenience in constructing.
     */
    public final Gregtech_MetaItem setFluidContainerStats(int aMetaValue, long aCapacity, long aStacksize) {
        if (aMetaValue < 0 || aMetaValue >= mOffset + mEnabledItems.length()) return this;
        if (aCapacity < 0) mElectricStats.remove((short) aMetaValue);
        else mFluidContainerStats.put((short) aMetaValue, new Long[]{aCapacity, Math.max(1, aStacksize)});
        return this;
    }

    /**
     * @return if this MetaGenerated Item should use my Default Renderer System.
     */
    public boolean useStandardMetaItemRenderer() {
        return true;
    }

    /**
     * @return the Color Modulation the Material is going to be rendered with.
     */
    public short[] getRGBa(ItemStack aStack) {
        return Materials._NULL.getRGBA();
    }

    /**
     * @return the Icon the Material is going to be rendered with.
     */
    public IIconContainer getIconContainer(int aMetaData) {
        return null;
    }
	
	/* ---------- INTERNAL OVERRIDES ---------- */

    @Override
    public ItemStack onItemRightClick(ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
        return super.onItemRightClick(aStack, aWorld, aPlayer);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack aStack) {
        return mFoodStats.get((short) getDamage(aStack)) == null ? 0 : 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack aStack) {
		return  EnumAction.none;
    }

    @Override
    public final ItemStack onEaten(ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
        IFoodStat tStat = mFoodStats.get((short) getDamage(aStack));
        if (tStat != null) {            
               
        }
        return aStack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item var1, CreativeTabs aCreativeTab, List aList) {
        for (int i = 0, j = mEnabledItems.length(); i < j; i++)
            if (mVisibleItems.get(i) || (D1 && mEnabledItems.get(i))) {
                Long[] tStats = mElectricStats.get((short) (mOffset + i));
                if (tStats != null && tStats[3] < 0) {
                    ItemStack tStack = new ItemStack(this, 1, mOffset + i);
                    setCharge(tStack, Math.abs(tStats[0]));
                    isItemStackUsable(tStack);
                    aList.add(tStack);
                }
                if (tStats == null || tStats[3] != -2) {
                    ItemStack tStack = new ItemStack(this, 1, mOffset + i);
                    isItemStackUsable(tStack);
                    aList.add(tStack);
                }
            }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public final void registerIcons(IIconRegister aIconRegister) {
        for (short i = 0, j = (short) mEnabledItems.length(); i < j; i++)
            if (mEnabledItems.get(i)) {
                for (byte k = 1; k < mIconList[i].length; k++) {
                    mIconList[i][k] = aIconRegister.registerIcon(CORE.MODID+":"+getUnlocalizedName() + "/" + i + "/" + k);
                }
                mIconList[i][0] = aIconRegister.registerIcon(CORE.MODID+":"+getUnlocalizedName() + "/" + i);
            }
    }

    @Override
    public final Long[] getElectricStats(ItemStack aStack) {
        return mElectricStats.get((short) aStack.getItemDamage());
    }

    @Override
    public final Long[] getFluidContainerStats(ItemStack aStack) {
        return mFluidContainerStats.get((short) aStack.getItemDamage());
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    public boolean isBookEnchantable(ItemStack aStack, ItemStack aBook) {
        return false;
    }

    @Override
    public boolean getIsRepairable(ItemStack aStack, ItemStack aMaterial) {
        return false;
    }
    
    @Override
	public int getColorFromItemStack(ItemStack stack, int HEX_OxFFFFFF) {	
    	if (stack.getDisplayName().contains("LuV")){
    		HEX_OxFFFFFF = 0xffffcc;
    	}
		else if (stack.getDisplayName().contains("ZPM")){
			HEX_OxFFFFFF = 0xace600;
		}
		else if (stack.getDisplayName().contains("UV")){
			HEX_OxFFFFFF = 0xffff00;
		}
		else if (stack.getDisplayName().contains("MAX")){
			HEX_OxFFFFFF = 0xff0000;
		}
		else {
			HEX_OxFFFFFF = 0xffffff;
		}		
		return HEX_OxFFFFFF;
	}
}