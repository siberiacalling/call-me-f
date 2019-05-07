cmake_minimum_required(VERSION 3.13)
project(untitled C)

set(CMAKE_C_STANDARD 99)

include_directories(/home/anita/mpich-install/include)
link_directories(/home/anita/mpich-install/lib)
add_executable(original main.c)
target_link_libraries (original mpi)
add_executable(my new_main.c)
target_link_libraries (my mpi)