cmake_minimum_required(VERSION 3.15)

add_executable(app ../app.cpp)

set(CMAKE_POLICY_DEFAULT_CMP0077 NEW) # required to make sure sets are taken into account

set(gRPC_INSTALL OFF)
set(gRPC_BUILD_GRPC_CPP_PLUGIN OFF)
set(gRPC_BUILD_GRPC_CSHARP_PLUGIN OFF)
set(gRPC_BUILD_GRPC_NODE_PLUGIN OFF)
set(gRPC_BUILD_GRPC_OBJECTIVE_C_PLUGIN OFF)
set(gRPC_BUILD_GRPC_PHP_PLUGIN OFF)
set(gRPC_BUILD_GRPC_PYTHON_PLUGIN OFF)
set(gRPC_BUILD_GRPC_RUBY_PLUGIN OFF)
set(gRPC_BUILD_CSHARP_EXT OFF)
set(gRPC_BUILD_CODEGEN OFF)

# see https://github.com/grpc/grpc/tree/master/examples/cpp/
include(FetchContent)
FetchContent_Declare(
        grpc
        GIT_REPOSITORY "https://github.com/grpc/grpc"
        GIT_TAG "868412b573a0663c8db41558498caf44098f4390" # 1.54.3 updated to protoc 3.21.12
)

FetchContent_MakeAvailable(grpc)

set(_PROTOBUF_LIBPROTOBUF libprotobuf)
set(_GRPC_GRPCPP grpc++)

# Include kaikosdk library
FetchContent_Declare(
        kaikosdk
        GIT_REPOSITORY "https://github.com/kaikodata/kaiko-cpp-sdk"
        GIT_TAG "v1.19.1"
)

FetchContent_GetProperties(kaikosdk)
if(NOT kaikosdk_POPULATED)
  FetchContent_Populate(kaikosdk)

  # Bring the populated content into the build
  include_directories(${kaikosdk_SOURCE_DIR}/src)
  add_subdirectory(${kaikosdk_SOURCE_DIR})
endif()

target_link_libraries(kaikosdk PRIVATE ${_PROTOBUF_LIBPROTOBUF} ${_GRPC_GRPCPP})
target_link_libraries(app PRIVATE kaikosdk ${_GRPC_GRPCPP})
