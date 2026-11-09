# Keep class/member names stable while evaluating R8's code shrinking and
# optimization. Commander uses several reflection-based Android integrations.
-dontobfuscate

# The icon browser enumerates these classes from the APK's DEX and loads
# their public vector getters by reflection. Without this rule R8 removes
# most of the selectable Phosphor icons.
-keep class com.adamglin.phosphoricons.duotone.** { *; }

# ShizukuBridge invokes this method by name; no direct call keeps it alive.
-keepclassmembers class rikka.shizuku.Shizuku {
    public static *** newProcess(java.lang.String[], java.lang.String[], java.lang.String);
}
