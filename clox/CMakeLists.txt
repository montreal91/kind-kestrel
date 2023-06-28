cmake_minimum_required(VERSION 3.0)

project(clox)

set(SOURCE_EXE
        src/chunk.c
        src/compiler.c
        src/debug.c
        src/main.c
        src/memory.c
        src/object.c
        src/scanner.c
        src/table.c
        src/value.c
        src/vm.c
 )

add_executable(clox ${SOURCE_EXE})
