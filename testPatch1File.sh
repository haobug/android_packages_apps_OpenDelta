ndk-build && \ant debug && \
if true;
then
ant debug install
adbam eu.chainfire.opendelta
else
adbshell sync
adbshell rm /system/app/OpenDelta-debug.apk && \
adbshell rm /system/lib/libopendelta.so && \
adbshell rm /system/lib/libapplypatch.so && \
adbpush libs/armeabi-v7a/libopendelta.so /system/app/
adbpush libs/armeabi-v7a/libapplypatch.so /system/app/
adbpush bin/OpenDelta-debug.apk /system/app/
fi
