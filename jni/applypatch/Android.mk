# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := applypatch.c bspatch.c freecache.c imgpatch.c utils.c applypatch_jni.c main.c 
LOCAL_MODULE := libapplypatch
LOCAL_MODULE_TAGS := eng
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS:=-L$(SYSROOT)/usr/lib -llog
LOCAL_STATIC_LIBRARIES += libmtdutils libmincrypt libbz  libminelf libz libedify
include $(BUILD_SHARED_LIBRARY)

include $(LOCAL_PATH)/mtdutils/Android.mk \
	$(LOCAL_PATH)/mincrypt/Android.mk \
	$(LOCAL_PATH)/minelf/Android.mk \
	$(LOCAL_PATH)/bzip2/Android.mk \
	$(LOCAL_PATH)/zlib/Android.mk \
	$(LOCAL_PATH)/edify/Android.mk
	

