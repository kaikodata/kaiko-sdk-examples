#!/bin/bash

set -e
# set -x

BUILD_TEST_PATH="./build_conan"
INCLUDE_DIR="conan"

rm -rf $BUILD_TEST_PATH
mkdir -p $BUILD_TEST_PATH

conan install . --build=missing --output=build_conan

pushd $BUILD_TEST_PATH

# install app with default compiler to work with libstdc++11 (protobuf requirement), otherwise linking fails.

CMAKE=$(find $(conan cache path $(conan list cmake/3.28.1:* --cache --format=compact | grep -E "cmake/[0-9.]+#[0-9a-z]+:[0-9a-z]+")) -type f -executable -name cmake)
$CMAKE .. -DCMAKE_BUILD_TYPE=Release -DCMAKE_TOOLCHAIN_FILE=conan_toolchain.cmake -DCMAKE_BINARY_DIR=$BUILD_TEST_PATH -DINCLUDE_DIR=$INCLUDE_DIR
$CMAKE --build .

popd

if [[ -z "$INSECURE_GRPC_BUILD" ]]; then
    echo "Removing unsecure grpc from linking"

    LINK_FILE_PATH="$BUILD_TEST_PATH/CMakeFiles/app.dir/link.txt"

    sed -i -r "s|/[^ ]+libgrpc[+]*_unsecure\.a[^ ]*||g" $LINK_FILE_PATH

    echo "Rebuilding"
    (cd $BUILD_TEST_PATH && make all)
fi
