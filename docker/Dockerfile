FROM gradle:9.1.0-jdk21

RUN apt-get update
RUN apt-get install -y git
RUN rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
RUN git config --global --add safe.directory /workspace

CMD ["/bin/bash"]
