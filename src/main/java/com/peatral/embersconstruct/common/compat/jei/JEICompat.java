package com.peatral.embersconstruct.common.compat.jei;

import com.peatral.embersconstruct.common.EmbersConstructItems;
import com.peatral.embersconstruct.common.inventory.ContainerKiln;
import com.peatral.embersconstruct.client.gui.GuiKiln;
import com.peatral.embersconstruct.common.EmbersConstructBlocks;
import com.peatral.embersconstruct.common.compat.jei.categories.KilnRecipeCategory;
import com.peatral.embersconstruct.common.compat.jei.wrapper.KilnRecipeWrapper;
import com.peatral.embersconstruct.common.registry.KilnRecipes;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import java.util.IllegalFormatException;

@JEIPlugin
public class JEICompat implements IModPlugin {
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        StampSubtypeInterpreter stampSubtypeInterpreter = new StampSubtypeInterpreter();
        subtypeRegistry.registerSubtypeInterpreter(EmbersConstructItems.Stamp, stampSubtypeInterpreter);
        subtypeRegistry.registerSubtypeInterpreter(EmbersConstructItems.StampRaw, stampSubtypeInterpreter);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        final IJeiHelpers helpers = registry.getJeiHelpers();
        final IGuiHelper gui = helpers.getGuiHelper();

        registry.addRecipeCategories(new KilnRecipeCategory(gui));
    }

    @Override
    public void register(IModRegistry registry) {
        final IIngredientRegistry ingredientRegistry = registry.getIngredientRegistry();
        final IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        IRecipeTransferRegistry recipeTransfer = registry.getRecipeTransferRegistry();

        registry.handleRecipes(KilnRecipes.Recipe.class, KilnRecipeWrapper::new, RecipeCategories.KILN);

        registry.addRecipes(KilnRecipes.instance().getRecipeList(), RecipeCategories.KILN);
        registry.addRecipeClickArea(GuiKiln.class, 78, 32, 28, 23, RecipeCategories.KILN, VanillaRecipeCategoryUid.FUEL);

        registry.addRecipeCatalyst(new ItemStack(EmbersConstructBlocks.Kiln), RecipeCategories.KILN, VanillaRecipeCategoryUid.FUEL);



        recipeTransfer.addRecipeTransferHandler(ContainerKiln.class, RecipeCategories.KILN, 0, 1, 3, 36);

    }

    public static String translateToLocal(String key) {
        if (I18n.canTranslate(key)) return I18n.translateToLocal(key);
        return I18n.translateToFallback(key);
    }

    public static String translateToLocalFormatted(String key, Object... format) {
        String s = translateToLocal(key);
        try {
            return String.format(s, format);
        } catch (IllegalFormatException e) {
            return "Format error: " + s;
        }
    }

}
