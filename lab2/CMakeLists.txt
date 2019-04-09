cmake_minimum_required(VERSION 3.13)
project(labs C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fsanitize=address -fno-omit-frame-pointer -g")
add_executable(labs ./lab2/var23_thread.c ./lab2/sts_queue.c)