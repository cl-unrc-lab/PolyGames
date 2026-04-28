# Use Fedora 40 as the base image (includes GCC 13.x)
FROM registry.fedoraproject.org/fedora:38

# Install GCC and related development tools
RUN dnf -y update && \
    dnf -y install gcc gcc-c++ make && \
    dnf clean all

# Verify the GCC version during build
RUN gcc --version

# Set the default command
CMD ["/bin/bash"]
