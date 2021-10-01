cmake_minimum_required(VERSION 3.15)

project(testsdk)

include(${CMAKE_BINARY_DIR}/conanbuildinfo.cmake)

add_executable(app app.cpp)

# Include kaikosdk library
# TODO: publish it as a prebuilt package on conan-center to cut compilation times
include(FetchContent)
FetchContent_Declare(
        kaikosdk
        GIT_REPOSITORY "https://github.com/challengerdeep/kaiko-cpp-sdk"
        GIT_TAG "v0.0.37"
)

FetchContent_GetProperties(kaikosdk)
if(NOT kaikosdk_POPULATED)
  FetchContent_Populate(kaikosdk)

  # Bring the populated content into the build
  include_directories(${kaikosdk_SOURCE_DIR}/src)
  add_subdirectory(${kaikosdk_SOURCE_DIR}/src)
endif()

conan_basic_setup(TARGETS)
target_link_libraries(app PRIVATE kaikosdk CONAN_PKG::grpc)
