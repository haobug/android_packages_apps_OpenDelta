ndk-build && ant debug && \
if false;
then
ant debug install
else
adbshell sync
adbshell mount -orw,remount /system
adbshell rm /system/app/OpenDelta-debug.apk && \
adbshell rm /system/app/libopendelta.so && \
adbshell rm /system/app/libapplypatch.so && \
adbshell rm /system/lib/libopendelta.so && \
adbshell rm /system/lib/libapplypatch.so && \
adbpush libs/armeabi-v7a/libopendelta.so /system/lib/
adbpush libs/armeabi-v7a/libapplypatch.so /system/lib/
adbpush bin/OpenDelta-debug.apk /system/app/
fi
adbam eu.chainfire.opendelta
