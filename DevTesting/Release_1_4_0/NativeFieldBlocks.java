package com.local.pickpocket;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
final class NativeFieldBlocks {
 static BlockSection section(World w,int x,int y,int z){var cs=w.getChunkStore();var ref=cs.getChunkSectionReferenceAtBlock(x,y,z);if(ref==null||!ref.isValid())throw new IllegalStateException("Unloaded probe section");return cs.getStore().getComponent(ref,BlockSection.getComponentType());}
 static BlockType type(World w,int x,int y,int z){return BlockType.getAssetMap().getAsset(section(w,x,y,z).get(x,y,z));}
 static int filler(World w,int x,int y,int z){return section(w,x,y,z).getFiller(x,y,z);}
 static void set(World w,int x,int y,int z,String id){var cs=w.getChunkStore();var ref=cs.getChunkSectionReferenceAtBlock(x,y,z);var map=BlockType.getAssetMap();BlockOperations.setBlock(cs,ref,x,y,z,map.getIndex(id),map.getAsset(id),0,0,0);}
}
