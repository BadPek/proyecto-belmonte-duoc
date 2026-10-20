# Reglas de ProGuard / R8 para modo release
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
