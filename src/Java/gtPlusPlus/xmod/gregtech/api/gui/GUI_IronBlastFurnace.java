package gtPlusPlus.xmod.gregtech.api.gui;

import gregtech.api.gui.GT_GUIContainerMetaTile_Machine;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gtPlusPlus.core.lib.CORE;
import net.minecraft.entity.player.InventoryPlayer;

public class GUI_IronBlastFurnace
        extends GT_GUIContainerMetaTile_Machine {
    public GUI_IronBlastFurnace(InventoryPlayer aInventoryPlayer, IGregTechTileEntity aTileEntity) {
        super(new CONTAINER_IronBlastFurnace(aInventoryPlayer, aTileEntity), CORE.RES_PATH_GUI+"IronBlastFurnace.png");
    }

    @Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        this.fontRendererObj.drawString("Iron Blast Furnace", 8, 4, 4210752);
    }

    @Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        super.drawGuiContainerBackgroundLayer(par1, par2, par3);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
        if ((this.mContainer != null) &&
                (this.mContainer.mProgressTime > 0)) {
            drawTexturedModalRect(x + 58, y + 28, 176, 0, Math.max(0, Math.min(20, (this.mContainer.mProgressTime > 0 ? 1 : 0) + this.mContainer.mProgressTime * 20 / (this.mContainer.mMaxProgressTime < 1 ? 1 : this.mContainer.mMaxProgressTime))), 11);
        }
    }
}
