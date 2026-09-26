package com.endra.clean;

/** One user-installed application eligible for cache inspection and selection. */
final class AppEntry {
    final String packageName;
    final String label;
    final long cacheBytes;
    boolean selected;

    AppEntry(String packageName, String label, long cacheBytes) {
        this.packageName = packageName;
        this.label = label;
        this.cacheBytes = cacheBytes;
    }
}
