package com.local.pickpocket;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.*;
import java.util.*;

public final class ContainerProbeTest {
  static ItemStack item(String id, int amount) { return new ItemStack(id, amount); }
  static void run(String name, int expected, java.util.function.BiConsumer<SimpleItemContainer,SimpleItemContainer> action) {
    var chest = new SimpleItemContainer((short)4);
    var player = new SimpleItemContainer((short)4);
    chest.setItemStackForSlot((short)0,item("Ingredient_Bar_Copper",10));
    player.setItemStackForSlot((short)1,item("Ingredient_Bar_Iron",3));
    var results = new ArrayList<ContainerRemovalProbe.Removal>();
    var probe = new ContainerRemovalProbe(chest,results::add);
    var a=chest.registerChangeEvent(e-> { System.out.println(name+" chest "+e.transaction());probe.source(e.transaction()); });
    var b=player.registerChangeEvent(e->probe.receipt(e.transaction()));
    action.accept(chest,player);
    probe.finish(); probe.finish();
    int quantity=results.stream().mapToInt(ContainerRemovalProbe.Removal::amount).sum();
    var notifications=new ArrayList<com.hypixel.hytale.server.core.Message>();
    var confirmed=results.stream().map(r->new ContainerVisualWitness.Evidence(null,"test-witness",1,true,true,false,null,"seen",null,0)).toList();
    ContainerCaughtNotification.notifyWitnessed(confirmed,notifications::add);
    if(notifications.size()!=(expected>0?1:0))throw new AssertionError(name+": incorrect caught notification count "+notifications.size());
    a.unregister(); b.unregister();
    if(quantity!=expected)throw new AssertionError(name+": expected "+expected+" got "+quantity+" "+results);
    System.out.println("PASS "+name+" removed="+quantity+" reports="+results.size());
  }
  public static void main(String[] args) {
    try { matrix(); } catch (Throwable e) {
      e.printStackTrace(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.err)));
      System.exit(1);
    }
  }
  static void matrix() {
    run("full",10,(c,p)->c.moveItemStackFromSlotToSlot((short)0,10,p,(short)0));
    run("partial",4,(c,p)->c.moveItemStackFromSlotToSlot((short)0,4,p,(short)0));
    run("quick",10,(c,p)->c.moveItemStackFromSlot((short)0,10,p));
    run("insert",0,(c,p)->p.moveItemStackFromSlotToSlot((short)1,3,c,(short)1));
    run("internal",0,(c,p)->c.moveItemStackFromSlotToSlot((short)0,10,c,(short)2));
    run("player-only",0,(c,p)->p.moveItemStackFromSlotToSlot((short)1,3,p,(short)2));
    run("swap-out",10,(c,p)->c.moveItemStackFromSlotToSlot((short)0,10,p,(short)1));
    run("swap-in",10,(c,p)->p.moveItemStackFromSlotToSlot((short)1,3,c,(short)0));
    run("failed",0,(c,p)->c.moveItemStackFromSlotToSlot((short)0,100,p,(short)0));
    run("sort",0,(c,p)->c.sortItems(com.hypixel.hytale.server.core.inventory.container.SortType.NAME));
    run("direct-drop",0,(c,p)->c.removeItemStackFromSlot((short)0,4));
    run("rapid",10,(c,p)->{for(int i=0;i<10;i++)c.moveItemStackFromSlotToSlot((short)0,1,p,(short)0);});
    twoPlayers();
    combined();
  }
  static void twoPlayers() {
    var chest=new SimpleItemContainer((short)2); var a=new SimpleItemContainer((short)2);
    var b=new SimpleItemContainer((short)2); var viewer=new SimpleItemContainer((short)2);
    chest.setItemStackForSlot((short)0,item("Ingredient_Bar_Copper",10));
    var ra=new ArrayList<ContainerRemovalProbe.Removal>(); var rb=new ArrayList<ContainerRemovalProbe.Removal>();
    var rv=new ArrayList<ContainerRemovalProbe.Removal>();
    var pa=new ContainerRemovalProbe(chest,ra::add); var pb=new ContainerRemovalProbe(chest,rb::add);
    var pv=new ContainerRemovalProbe(chest,rv::add);
    var hooks=List.of(chest.registerChangeEvent(e->{pa.source(e.transaction());pb.source(e.transaction());pv.source(e.transaction());}),
        a.registerChangeEvent(e->pa.receipt(e.transaction())),b.registerChangeEvent(e->pb.receipt(e.transaction())),
        viewer.registerChangeEvent(e->pv.receipt(e.transaction())));
    chest.moveItemStackFromSlotToSlot((short)0,4,a,(short)0);
    chest.moveItemStackFromSlotToSlot((short)0,6,b,(short)0);
    pa.finish();pb.finish();pv.finish(); for(var h:hooks)h.unregister();
    if(ra.size()!=1||ra.getFirst().amount()!=4||rb.size()!=1||rb.getFirst().amount()!=6||!rv.isEmpty())
      throw new AssertionError("Two recipients / untouched viewer attribution failed");
    System.out.println("PASS two-player native-receipt-isolation A=4 B=6 other-viewer=0 (network/UI test still manual)");
  }
  static void combined() {
    var chest=new SimpleItemContainer((short)2);var a=new SimpleItemContainer((short)1);var b=new SimpleItemContainer((short)2);
    chest.setItemStackForSlot((short)0,item("Ingredient_Bar_Copper",10));
    int max=item("Ingredient_Bar_Copper",1).getItem().getMaxStack();
    a.setItemStackForSlot((short)0,item("Ingredient_Bar_Copper",max-2));
    var results=new ArrayList<ContainerRemovalProbe.Removal>();var probe=new ContainerRemovalProbe(chest,results::add);
    var hooks=List.of(chest.registerChangeEvent(e->probe.source(e.transaction())),a.registerChangeEvent(e->probe.receipt(e.transaction())),
        b.registerChangeEvent(e->probe.receipt(e.transaction())));
    chest.moveItemStackFromSlot((short)0,10,new CombinedItemContainer(a,b));probe.finish();
    for(var h:hooks)h.unregister();
    if(results.stream().mapToInt(ContainerRemovalProbe.Removal::amount).sum()!=10)throw new AssertionError("Combined receipt remapping failed");
    System.out.println("PASS combined-player-inventory removed=10 reports="+results.size());
  }
}
