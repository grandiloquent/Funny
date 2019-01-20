cmake_minimum_required(VERSION 3.4.1)

set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution)

add_library(libiconv SHARED IMPORTED)
set_target_properties(libiconv PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libiconv/lib/${ANDROID_ABI}/libiconv.so)

add_library(main SHARED
            main.c)

target_include_directories(main PRIVATE
        ${distribution_DIR}/libiconv/include)

target_link_libraries(main
                      android
                      libiconv
                      log)