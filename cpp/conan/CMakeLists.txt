cmake_minimum_required(VERSION 3.15)

include(${CMAKE_BINARY_DIR}/conanbuildinfo.cmake)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin) # change binary dir to "bin"

add_executable(app ../app.cpp)

# Include kaikosdk library
# TODO: publish it as a prebuilt package on conan-center to cut compilation times
include(FetchContent)
FetchContent_Declare(
        kaikosdk
        GIT_REPOSITORY "https://github.com/kaikodata/kaiko-cpp-sdk"
        GIT_TAG "v1.14.1"
)

FetchContent_GetProperties(kaikosdk)
if(NOT kaikosdk_POPULATED)
  FetchContent_Populate(kaikosdk)

  # Bring the populated content into the build
  include_directories(${kaikosdk_SOURCE_DIR}/src)
  add_subdirectory(${kaikosdk_SOURCE_DIR}/src)

  target_link_libraries(kaikosdk CONAN_PKG::protobuf CONAN_PKG::grpc) # this is needed while it's not published as a proper conan lib, otherwise conan can't resolve inner dependencies
endif()

conan_basic_setup(TARGETS)
target_link_libraries(app kaikosdk CONAN_PKG::grpc)
