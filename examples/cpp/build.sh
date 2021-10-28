#!/bin/bash

# set -e
# set -x

mkdir -p build

pushd build

# remove only build files, keep CMake caches
(rm -f * 2> /dev/null || true) && rm -rf build && rm -rf CMakeFiles

cmake .. -DFETCHCONTENT_QUIET=OFF -DCMAKE_BUILD_TYPE=Release
cmake --build .

popd
