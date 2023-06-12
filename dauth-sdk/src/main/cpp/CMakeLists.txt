cmake_minimum_required(VERSION 3.26.4)

project("hello-jni")

# Sets lib_build_DIR to the path of the desired output directory.
set( lib_build_DIR ../jniLibs )
file(MAKE_DIRECTORY ${lib_build_DIR})

add_library(hello-jni SHARED
            hello-jni.cpp)

# Include libraries needed for hello-jni lib
target_link_libraries(hello-jni
                      android
                      log)



