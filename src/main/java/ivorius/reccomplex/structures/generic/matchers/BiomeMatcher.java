/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.json.RCGsonHelper;
import ivorius.reccomplex.utils.ExpressionCaches;
import ivorius.reccomplex.utils.PrefixedTypeExpressionCache;
import ivorius.reccomplex.utils.RCBoolAlgebra;
import joptsimple.internal.Strings;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * Created by lukas on 19.09.14.
 */
public class BiomeMatcher extends PrefixedTypeExpressionCache<Boolean> implements Predicate<BiomeGenBase>
{
    public static final String BIOME_TYPE_PREFIX = "$";

    public BiomeMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, EnumChatFormatting.GREEN + "Any Biome", expression);

        addType(new BiomeVariableType(""));
        addType(new BiomeDictVariableType(BIOME_TYPE_PREFIX));
    }

    public static String ofTypes(BiomeDictionary.Type... biomeTypes)
    {
        return BIOME_TYPE_PREFIX + String.join(" & " + BIOME_TYPE_PREFIX, Lists.transform(Arrays.asList(biomeTypes), input -> input != null ? IvGsonHelper.serializedName(input) : null));
    }

    public static Set<BiomeGenBase> gatherAllBiomes()
    {
        Set<BiomeGenBase> set = new HashSet<>();

        for (BiomeGenBase biomeGenBase : BiomeGenBase.getBiomeGenArray())
        {
            if (biomeGenBase != null)
                set.add(biomeGenBase);
        }

        for (BiomeDictionary.Type type : BiomeDictionary.Type.values())
        {
            try
            {
                Collections.addAll(set, BiomeDictionary.getBiomesForType(type));
            }
            catch (Exception ignored) // list f'd up by a biome mod
            {

            }
        }

        return set;
    }

    @Override
    public boolean containsUnknownVariables()
    {
        return super.containsUnknownVariables(gatherAllBiomes());
    }

    @Nonnull
    @Override
    public String getDisplayString()
    {
        return super.getDisplayString(gatherAllBiomes());
    }

    @Override
    public boolean apply(final BiomeGenBase input)
    {
        return evaluate(input);
    }

    protected static class BiomeVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public BiomeVariableType(String prefix)
        {
            super(prefix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            return ((BiomeGenBase) args[0]).biomeName.equals(var);
        }

        @Override
        public boolean isKnown(final String var, final Object... args)
        {
            if (args[0] instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) args[0];
                return StreamSupport.stream(iterable.spliterator(), false).anyMatch(input -> input instanceof BiomeGenBase && ((BiomeGenBase) input).biomeName.equals(var));
            } else {
                return false;
            }
        }
    }

    protected static class BiomeDictVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public BiomeDictVariableType(String prefix)
        {
            super(prefix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            BiomeDictionary.Type type = RCGsonHelper.enumForNameIgnoreCase(var, BiomeDictionary.Type.values());
            return type != null && BiomeDictionary.isBiomeOfType((BiomeGenBase) args[0], type);
        }

        @Override
        public boolean isKnown(String var, Object... args)
        {
            return RCGsonHelper.enumForNameIgnoreCase(var, BiomeDictionary.Type.values()) != null;
        }
    }
}
