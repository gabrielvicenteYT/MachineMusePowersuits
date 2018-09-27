package net.machinemuse.numina.recipe;

import net.machinemuse.numina.utils.MuseLogger;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MachineMuse (Claire Semple)
 * Created: 2:45 PM, 11/4/13
 */
public class JSONRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements net.minecraftforge.common.crafting.IShapedRecipe {
    public SimpleItemMatcher[][] ingredients;
    public SimpleItemMaker result;
    public Boolean mirror;
    private boolean isValid = true;

    static final int MAX_WIDTH = 3;
    static final int MAX_HEIGHT = 3;

    public boolean getIsValid(){
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public int getRecipeWidth() {
        return getWidth();
    }

    @Override
    public int getRecipeHeight() {
        return ingredients.length;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        int width = getWidth();
        int height = ingredients.length;
        for (int xoffset = 0; xoffset <= MAX_WIDTH - width; xoffset++) {
            for (int yoffset = 0; yoffset <= MAX_HEIGHT - height; yoffset++) {
                if (matchSpot(inv, xoffset, yoffset, width, height, false)) {
                    return true;
                }
                if (mirror != null && mirror && matchSpot(inv, xoffset, yoffset, width, height, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        return result.makeItem(inventoryCrafting);
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= getRecipeWidth() && height >= getRecipeHeight();
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        if (result.getRecipeOutput() == null)
            setIsValid(false);
        return result.getRecipeOutput();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);
            nonnulllist.set(i, ForgeHooks.getContainerItem(itemstack));
        }
        return nonnulllist;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return null;
    }

    public void validate() {
        List<ItemStack> inputs = new ArrayList<>();
        int height = ingredients.length;
        int width = getWidth();

        if (height == 0 || width == 0) {
            isValid = false;
            return;
        }
        if (result == null) {
            isValid = false;
            return;
        }
        for (int getY = 0; getY < height; getY++) {
            if (ingredients[getY] != null) {
                for (int getX = 0; getX < width; getX++) {
                    List<ItemStack> itemStacks = new ArrayList<>();
                    if (ingredients[getY].length > getX) itemStacks = getIngredient(ingredients[getY][getX]);
                    if (itemStacks != null && itemStacks.isEmpty()) {
                        isValid = false;
                    }
                }
            }
        }
    }

    private boolean matchSpot(InventoryCrafting inv, int xoffset, int yoffset, int width, int height, boolean mirror) {
        boolean mismatch = false;
        for (int xinventory = 0; xinventory < MAX_WIDTH; xinventory++) {
            for (int yinventory = 0; yinventory < MAX_HEIGHT; yinventory++) {
                SimpleItemMatcher matcher;
                if (!mirror) {
                    matcher = getMatcher(-xoffset + xinventory, -yoffset + yinventory);
                } else {
                    matcher = getMatcher(-xoffset + width - xinventory - 1, -yoffset + yinventory);
                }
                if (matcher != null && !matcher.matchesItem(getInvStack(inv, xinventory, yinventory))) {
                    mismatch = true;
                }
                if (matcher == null && getInvStack(inv, xinventory, yinventory) != null) {
                    mismatch = true;
                }
            }
        }
        return !mismatch;
    }

    private ItemStack getInvStack(InventoryCrafting inv, int getX, int getY) {
        if (getX < 0 || getY < 0) {
            return null;
        } else {
            return inv.getStackInRowAndColumn(getX, getY);
        }
    }

    public SimpleItemMatcher getMatcher(int getX, int getY) {
        if (getY >= 0 && getY < ingredients.length) {
            if (getX >= 0 && getX < ingredients[getY].length) {
                return ingredients[getY][getX];
            }
        }
        return null;
    }

    public int getWidth() {
        int size = 0;
        for (IItemMatcher[] row : ingredients) {
            if (row != null) {
                size = Math.max(row.length, size);
            }
        }
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof JSONRecipe)) {
            return false;
        }
        JSONRecipe other = (JSONRecipe) obj;
        if(ingredients.length != other.ingredients.length) return false;
        for(int i = 0; i<ingredients.length;i++) {
            if(ingredients[i].length != other.ingredients[i].length) return false;
            for(int j=0; j<ingredients[i].length;j++) {

                if(!compareIngredients(ingredients[i][j], other.ingredients[i][j])) {
                    return false;
                }
            }
        }
        if(!compareResult(result, other.result)) return false;
        return compareBoolean(mirror, other.mirror);
    }

    private boolean compareResult(SimpleItemMaker a, SimpleItemMaker b) {
        if(a == null && b == null) return true;
        if(a != null && b != null) {
            if(a.equals(b)) return true;
        }
        return false;
    }
    private boolean compareIngredients(SimpleItemMatcher a, SimpleItemMatcher b) {
        if(a == null && b == null) return true;
        if(a != null && b != null) {
            if(a.equals(b)) return true;
        }
        return false;
    }

    private boolean compareBoolean(Boolean a, Boolean b) {
        if(a == null && b == null) return true;
        if(a != null && b != null) {
            if(a.booleanValue() == b.booleanValue()) return true;
        }
        return false;
    }

    public List<ItemStack> getIngredient(SimpleItemMatcher cell) {
        Boolean shouldbenull = true;
        List<ItemStack> result = null;
        if (cell == null) {
            return null;
        }

        if (cell.oredictName != null) {
            shouldbenull = false;
            result = OreDictionary.getOres(cell.oredictName);

            if (cell.meta != null && result != null && cell.meta != OreDictionary.WILDCARD_VALUE) {
                ArrayList<ItemStack> t = new ArrayList<ItemStack>();
                for (ItemStack stack : result)
                    if (cell.meta == stack.getItemDamage())
                        t.add(stack);
                result = t;
            }
        }

        if (cell.itemStackName != null) {
            shouldbenull = false;
            result = new ArrayList<>();

            ItemStack stack = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(cell.itemStackName)), 1);
            if(stack != null) {
                stack = stack.copy();
                if(cell.meta != null) {
                    stack.setItemDamage(cell.meta);
                }
                result.add(stack);
            }
        }

        if(cell.registryName != null) {
            shouldbenull = false;
            result = new ArrayList<>();
            Item item = Item.REGISTRY.getObject(new ResourceLocation(cell.registryName));
            if(item != null) {
                int newMeta = cell.meta == null ? 0 : cell.meta;
                ItemStack stack = new ItemStack(item, 1, newMeta);
                result.add(stack);
            }
        }

        if (cell.nbtString != null && result != null) {
            shouldbenull = false;
            ArrayList<ItemStack> t = new ArrayList<ItemStack>();
            for (ItemStack stack : result) {
                ItemStack stack2 = stack.copy();
                try {
                    stack2.setTagCompound(JsonToNBTFixed.getTagFromJson(cell.nbtString));
                } catch (NBTException e) {
                    e.printStackTrace();
                }
                t.add(stack2);
            }
            result = t;
        }
        // this provides some basic info on an invalid recipe cell
        if (!shouldbenull && (result == null || result.size() == 0)) {
            this.isValid = false;
            MuseLogger.logError("cell should not be null but it is");
            MuseLogger.logError("cell.oredictName: " + cell.oredictName);
            MuseLogger.logError("cell.itemStackName: " + cell.itemStackName);
            MuseLogger.logError("cell.registryName: "+ cell.registryName);
            MuseLogger.logError("cell.nbtString: " + cell.nbtString);
        }
        return result;
    }
}