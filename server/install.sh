DIR_BASE=$1
if test -z ${DIR_BASE};
then 
    DIR_BASE=/home2/tmp/merge_delta2
fi
export DIR_BASE
mkdir -p ${DIR_BASE}
cd ${DIR_BASE}
git clone ckt@10.120.0.151:/home2/tmp/merge_delta/android_packages_apps_OpenDelta -b local_test ${DIR_BASE}/android_packages_apps_OpenDelta
cd android_packages_apps_OpenDelta/jni
mkdir -p ${DIR_BASE}/build/delta
 
#compile xdelta3
cd ${DIR_BASE}/android_packages_apps_OpenDelta/jni
cd xdelta3-3.0.7
sh ./configure && make -j9
ln -sf `pwd`/xdelta3 ${DIR_BASE}/build/delta/
 
#zipadjust
cd ${DIR_BASE}/android_packages_apps_OpenDelta/jni
gcc -o zipadjust zipadjust.c zipadjust_run.c -lz
ln -sf `pwd`/zipadjust ${DIR_BASE}/build/delta/
 
#dedelta
cd ${DIR_BASE}/android_packages_apps_OpenDelta/jni
gcc -o dedelta xdelta3-3.0.7/xdelta3.c delta.c delta_run.c
ln -sf `pwd`/dedelta ${DIR_BASE}/build/delta/
 
#minsignapk
cd ${DIR_BASE}/android_packages_apps_OpenDelta/server
ln -sf `pwd`/minsignapk ${DIR_BASE}/build/delta/
 
#创建几个需要的目录
mkdir ${DIR_BASE}/build/delta/{last,curr,full}
