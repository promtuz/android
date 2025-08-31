cmake_minimum_required(VERSION 3.22.1)
project("core_bridge")

add_library(core_bridge SHARED wasm-core.cpp)

# Add 16KB alignment flags for Android 15+ compatibility
if (ANDROID_PLATFORM_LEVEL GREATER_EQUAL 35)  # Android 15 is API level 35
    target_link_options(core_bridge PRIVATE
            -Wl,-z,max-page-size=16384
            -Wl,-z,common-page-size=16384
    )
else ()
    # Fallback for older Android versions
    target_link_options(core_bridge PRIVATE
            -Wl,-z,max-page-size=16384
            -Wl,-z,common-page-size=4096
    )
endif ()

# Import the pre-built Rust library
add_library(wasm_core SHARED IMPORTED)
set_target_properties(wasm_core PROPERTIES
        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libwasm_core.so
)

# Link the Rust library to our bridge
target_link_libraries(core_bridge wasm_core log)

# Include directories
target_include_directories(core_bridge PRIVATE ${CMAKE_SOURCE_DIR})