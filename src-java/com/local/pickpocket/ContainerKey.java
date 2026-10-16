package com.local.pickpocket;

/** Persistent world/block identity; never an ownership registry. */
public record ContainerKey(java.util.UUID world, int x, int y, int z) {}
