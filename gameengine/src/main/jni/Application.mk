APP_ABI := armeabi-v7a arm64-v8a
APP_PLATFORM := android-21
APP_STL := c++_static
# NDK r23+ defaults to 16 KB ELF LOAD-segment alignment; the r21-era manual
# "-Wl,-z,max-page-size=16384" workaround is no longer needed as of the NDK
# bump in Phase 1 of docs/GAMEENGINE_UPGRADE_PLAN.md. Verified via
# `readelf -l` on the rebuilt .so (see that plan's Phase 1 exit criteria).
