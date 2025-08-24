FROM maven:3.9.5-eclipse-temurin-21-alpine

# Create a user with UID 1000 to match your host user
RUN groupadd -g 1000 vscode && \
    useradd -u 1000 -g 1000 -m -s /bin/bash vscode && \
    mkdir -p /home/vscode/.vscode-server && \
    chown -R vscode:vscode /home/vscode

# Install additional tools that might be useful
RUN apt-get update && apt-get install -y \
    git \
    curl \
    vim \
    sudo \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Add vscode user to sudoers
RUN echo "vscode ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

# Set the default user
USER vscode

# Set the working directory
WORKDIR /home/vscode/workspace
