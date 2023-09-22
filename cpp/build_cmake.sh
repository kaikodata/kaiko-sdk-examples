#!/bin/bash

set -e
# set -x

BUILD_TEST_PATH="./build_cmake"
INCLUDE_DIR="cmake"

mkdir -p $BUILD_TEST_PATH

pushd $BUILD_TEST_PATH

# remove only build files, keep CMake caches
(rm -f * 2> /dev/null || true) && rm -rf $BUILD_TEST_PATH && rm -rf CMakeFiles

cmake .. -DFETCHCONTENT_QUIET=OFF -DCMAKE_BUILD_TYPE=Release -DINCLUDE_DIR=$INCLUDE_DIR
cmake --build .

popd
