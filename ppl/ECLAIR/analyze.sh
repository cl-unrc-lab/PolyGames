#!/bin/sh

set -e

usage() {
    echo "Usage: analyze.sh ANALYSIS" 1>&2
    echo "  where ANALYSIS = BUGFIND" 1>&2
}

if [ $# -ne 1 ]
then
    usage
    exit 1
fi

case "$1" in
    MC2|MC3|BUGFIND)
    ;;
*)
    usage
    exit 1
esac

ANALYSIS=$1

export HERE="$(cd "$(dirname $0)" ; echo $PWD)"
TOP="$(dirname "${HERE}")"

export CC_ALIASES="gcc"
export CXX_ALIASES="g++"

export ECLAIR_PROJECT_NAME="PPL"
export ECLAIR_PROJECT_ROOT="/Users/pablo/University/Tools/PolyGames/ppl"

export ECLAIR_OUTPUT_DIR="${HERE}/out_${BUILD_ID}_${ANALYSIS}"
export ECLAIR_DATA_DIR="${ECLAIR_OUTPUT_DIR}/.data"
export PROJECT_ECD="${ECLAIR_DATA_DIR}/PROJECT.ecd"
rm -rf "$ECLAIR_OUTPUT_DIR"
mkdir -p "$ECLAIR_DATA_DIR"
export ECLAIR_DIAGNOSTICS_OUTPUT="$ECLAIR_OUTPUT_DIR/DIAGNOSTICS.txt"

(
    cd "/Users/pablo/University/Tools/PolyGames/ppl"
    make clean
)

if [ -f /proc/cpuinfo ]
then
  PROCESSORS=$(grep -c ^processor /proc/cpuinfo)
else
  PROCESSORS=6
fi

(
    cd "/Users/pablo/University/Tools/PolyGames/ppl"
    eclair_env "-eval_file='/Users/pablo/University/Tools/PolyGames/ppl/ECLAIR/analyze_${ANALYSIS}.ecl'" \
               -- make -j${PROCESSORS}
)

eclair_report "-create_db='$PROJECT_ECD'" "$ECLAIR_DATA_DIR"/FRAME.*.ecb \
              -load -reports1_txt=service,analysis.log
