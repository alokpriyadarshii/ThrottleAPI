package com.redstone.quotaguard.domain;

/**
 * Snapshot of bucket state.
 */
public record BucketStatus(double tokens, long capacity) {}
