package tauri.dev.jsg.integration.jei.category;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import tauri.dev.jsg.JSG;
import tauri.dev.jsg.block.JSGBlocks;
import tauri.dev.jsg.block.machine.CrystalChamberBlock;
import tauri.dev.jsg.integration.jei.AbstractJEIRecipe;
import tauri.dev.jsg.machine.chamber.CrystalChamberRecipe;

import javax.annotation.Nonnull;

public class JEIChamberRecipeCategory implements IRecipeCategory<AbstractJEIRecipe> {

    public static final String UID = "jsg_chamber";
    public static final ResourceLocation BACK_TEXTURE = new ResourceLocation(JSG.MOD_ID, "textures/gui/container_chamber_jei.png");

    public IDrawable background;
    public IDrawable icon;
    public IDrawable progressBar;
    public IDrawable fluidMeter;

    public JEIChamberRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(BACK_TEXTURE, 0, 0, 119, 55);
        this.fluidMeter = helper.createDrawable(BACK_TEXTURE, 176, 32, 16, 54);
        this.icon = helper.createDrawableIngredient(new ItemStack(JSGBlocks.MACHINE_CHAMBER));
        this.progressBar = helper.createAnimatedDrawable(helper.createDrawable(BACK_TEXTURE, 176, 0, 216 - 176, 15), 40, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Nonnull
    @Override
    public String getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return I18n.format("tile.jsg.crystal_chamber_block.name");
    }

    @Nonnull
    @Override
    public String getModName() {
        return JSG.MOD_NAME;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        fluidMeter.draw(minecraft, 46, 0);
        progressBar.draw(minecraft, 40, 19);
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull AbstractJEIRecipe crystalChamberRecipe, @Nonnull IIngredients ingredients) {
        // input slots
        recipeLayout.getItemStacks().init(0, true, 0, 18);
        // output slot
        recipeLayout.getItemStacks().init(1, false, 98, 18);
        recipeLayout.getItemStacks().set(ingredients);
        // fluid
        recipeLayout.getFluidStacks().init(2, true, 47, 1, 16, 53, CrystalChamberBlock.FLUID_CAPACITY, false, fluidMeter);
        recipeLayout.getFluidStacks().set(2, crystalChamberRecipe.getSubFluidStack());
    }
}
