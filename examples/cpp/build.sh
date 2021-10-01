#!/bin/bash

# set -e
# set -x

rm -rf build
mkdir build

pushd build

# install app with default compiler to work with libstdc++11 (protobuf requirement), otherwise linking fails.
conan install .. --build=missing -s compiler.libcxx=libstdc++11

CMAKE="$(conan info cmake/3.21.3@ --package-filter cmake* --path --only package_folder | grep package_folder | cut -d ":" -f2)/bin/cmake"
$CMAKE .. -DCMAKE_BUILD_TYPE=Release
$CMAKE --build .

popd

if [[ -z "$INSECURE_GRPC_BUILD" ]]; then
    echo "Removing unsecure grpc from linking"

    BUILD_TEST_PATH="./build"
    LINK_FILE_PATH="$BUILD_TEST_PATH/CMakeFiles/app.dir/link.txt"

    sed -i -r "s|/[^ ]+libgrpc[+]*_unsecure\.a[^ ]*||g" $LINK_FILE_PATH

    echo "Rebuilding"
    (cd $BUILD_TEST_PATH && make all)
fi
