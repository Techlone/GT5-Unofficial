package gregtech.api.enchants;

import gregtech.api.GTValues;
import gregtech.api.util.GTUtility;
import net.minecraft.enchantment.EnchantmentDamage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class EnchantmentRadioactivity extends EnchantmentDamage {

    public static final EnchantmentRadioactivity INSTANCE = new EnchantmentRadioactivity();

    private EnchantmentRadioactivity() {
        super(Rarity.VERY_RARE, 0);
    }

    public void register() {
        GameRegistry.register(this, new ResourceLocation(GTValues.MODID, "radioactivity"));
    }

    @Override
    public int getMinEnchantability(int level) {
        return 0;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return 0;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean canApply(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return false;
    }

    @Override
    public void onEntityDamaged(EntityLivingBase hurtEntity, Entity damagingEntity, int level) {
        GTUtility.applyRadioactivity(hurtEntity, level, 1);
    }

}