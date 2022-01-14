#!/bin/bash

set -e
# set -x

BUILD_TEST_PATH="./build_conan"
INCLUDE_DIR="conan"

rm -rf $BUILD_TEST_PATH
mkdir -p $BUILD_TEST_PATH

pushd $BUILD_TEST_PATH

# install app with default compiler to work with libstdc++11 (protobuf requirement), otherwise linking fails.
conan install .. --build=missing -s compiler.libcxx=libstdc++11

CMAKE="$(conan info cmake/3.22.0@ --package-filter cmake* --path --only package_folder | grep package_folder | cut -d ":" -f2)/bin/cmake"
$CMAKE .. -DCMAKE_BUILD_TYPE=Release -DCMAKE_BINARY_DIR=$BUILD_TEST_PATH -DINCLUDE_DIR=$INCLUDE_DIR
$CMAKE --build .

popd

if [[ -z "$INSECURE_GRPC_BUILD" ]]; then
    echo "Removing unsecure grpc from linking"

    LINK_FILE_PATH="$BUILD_TEST_PATH/CMakeFiles/app.dir/link.txt"

    sed -i -r "s|/[^ ]+libgrpc[+]*_unsecure\.a[^ ]*||g" $LINK_FILE_PATH

    echo "Rebuilding"
    (cd $BUILD_TEST_PATH && make all)
fi
