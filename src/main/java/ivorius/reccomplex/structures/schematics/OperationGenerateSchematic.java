/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.schematics;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.operation.Operation;
import ivorius.reccomplex.structures.OperationGenerateStructure;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Created by lukas on 10.02.15.
 */
public class OperationGenerateSchematic implements Operation
{
    public SchematicFile file;

    public BlockCoord lowerCoord;

    public OperationGenerateSchematic()
    {
    }

    public OperationGenerateSchematic(SchematicFile file, BlockCoord lowerCoord)
    {
        this.file = file;
        this.lowerCoord = lowerCoord;
    }

    @Override
    public void perform(World world)
    {
        file.generate(world, lowerCoord.x, lowerCoord.y, lowerCoord.z);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound fileCompound = new NBTTagCompound();
        file.writeToNBT(fileCompound);
        compound.setTag("schematic", fileCompound);

        BlockCoord.writeCoordToNBT("lowerCoord", lowerCoord, compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        try
        {
            file = new SchematicFile(compound.getCompoundTag("schematic"));
        }
        catch (SchematicFile.UnsupportedSchematicFormatException e)
        {
            e.printStackTrace();
            file = new SchematicFile((short) 0, (short) 0, (short) 0);
        }

        lowerCoord = BlockCoord.readCoordFromNBT("lowerCoord", compound);
    }

    @Override
    public void renderPreview(int previewType, World world, int ticks, float partialTicks)
    {
        if (previewType == PREVIEW_TYPE_BOUNDING_BOX)
            OperationGenerateStructure.maybeRenderBoundingBox(lowerCoord, new int[]{file.width, file.length, file.height}, ticks, partialTicks);
    }
}