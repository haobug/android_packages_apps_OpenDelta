# Copyright 2009 The Android Open Source Project

LOCAL_PATH := $(call my-dir)

edify_src_files := \
	expr.c

# "-x c" forces the lex/yacc files to be compiled as c;
# the build system otherwise forces them to be c++.
edify_cflags := -x c

#
# Build the device-side library
#
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(edify_src_files)

LOCAL_CFLAGS := $(edify_cflags)
LOCAL_MODULE := libedify

include $(BUILD_STATIC_LIBRARY)
