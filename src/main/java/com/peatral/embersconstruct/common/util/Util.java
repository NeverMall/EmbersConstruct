package com.peatral.embersconstruct.common.util;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import slimeknights.tconstruct.library.materials.Material;

public class Util {
    public static Fluid getFluidFromString(String s) {
        if (FluidRegistry.isFluidRegistered(s)) return FluidRegistry.getFluid(s);
        return null;
    }

    public static Fluid getFluidFromMaterial(Material material) {
        Fluid fluid = null;
        String[] names = new String[]{
                material.identifier,
                material.identifier + "fluid",
                material.identifier + "_fluid",
                "fluid" + material.identifier,
                "fluid_" + material.identifier
        };
        for (String s : names) {
            fluid = getFluidFromString(s);
            if (fluid != null) return fluid;
        }
        return null;
    }
}
