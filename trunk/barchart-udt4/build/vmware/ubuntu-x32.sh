#!/bin/bash

###############

THIS_PATH="$(dirname $0 | cd && pwd)"

###############

# 
source "$THIS_PATH/common.sh"

verify_tool_present "vmrun"


