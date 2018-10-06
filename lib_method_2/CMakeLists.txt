cmake_minimum_required(VERSION 3.4.1)


file(GLOB cpp_source_files "src/main/cpp/*.cpp")
file(GLOB c_source_files "src/main/cpp/*.c")
file(GLOB h_source_files "src/main/cpp/*.hpp")

add_library( # Sets the name of the library.
             native_hook

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${c_source_files}
             ${cpp_source_files}
             ${hpp_source_files}
              )

find_library(
              log-lib
              log )


target_link_libraries( # Specifies the target library.
                       native_hook
                       ${log-lib}
                       )