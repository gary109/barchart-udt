#!/bin/bash

# c
# sudo apt-get install gcc-multilib

# c++
# sudo apt-get install g++-multilib

g++ -m32 -o hello32 hello.cpp 

g++ -m64 -o hello64 hello.cpp

hello32

hello64


