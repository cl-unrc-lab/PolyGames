# syntax=docker/dockerfile:1
#
# Multi-stage build for PolyGames — an extension of PRISM-games
# that supports polyhedral probability distributions.
#
# Stage 1 (builder): compile PPL (with Java interface) + CUDD + polygames
# Stage 2 (runtime): minimal JRE image with compiled artifacts
#
# Build:  docker build -t polygames .
# Run:    docker run --rm polygames <model.prism> <props.file>
# Shell:  docker run --rm -it --entrypoint bash polygames

# ── builder ───────────────────────────────────────────────────────────────────
FROM ubuntu:22.04 AS builder

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
        openjdk-17-jdk \
        gcc \
        g++ \
        make \
        javacc \
        libgmp-dev \
        m4 \
        automake \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

WORKDIR /build

# ── PPL (Parma Polyhedra Library) with Java interface ─────────────────────────
# Copied first so this expensive step is cached independently of polygames changes.
# ppl_java_common.cc was fetched from https://github.com/BUGSENG/PPL and committed.
COPY ppl/ ./ppl/

RUN cd ppl && \
    ./configure \
        --enable-interfaces=java \
        --with-java="$JAVA_HOME" \
        --disable-documentation \
        CXXFLAGS=-std=c++11 && \
    make -j"$(nproc)" && \
    make install

# ── CUDD + polygames ──────────────────────────────────────────────────────────
COPY cudd/      ./cudd/
COPY polygames/ ./polygames/

RUN cd polygames && \
    make JAVA_DIR="$JAVA_HOME"

# ── runtime ───────────────────────────────────────────────────────────────────
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
        openjdk-17-jre \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /polygames

COPY --from=builder /build/polygames/bin/        ./bin/
COPY --from=builder /build/polygames/classes/    ./classes/
COPY --from=builder /build/polygames/lib/        ./lib/
COPY --from=builder /build/polygames/etc/        ./etc/
COPY --from=builder /build/polygames/dtds/       ./dtds/
COPY --from=builder /build/polygames/images/     ./images/
COPY --from=builder /build/polygames/polytests/  ./polytests/

# PPL main shared library (copied here for LD_LIBRARY_PATH to pick it up)
COPY --from=builder /usr/local/lib/libppl.so.*   ./lib/

# Fix the hard-coded build path in the launch script
RUN sed -i 's|PRISM_DIR=.*|PRISM_DIR="/polygames"|g' bin/polygames bin/prism

# Allow users to mount a directory with their own models
VOLUME ["/models"]

ENTRYPOINT ["/polygames/bin/polygames"]
CMD ["--help"]
