FROM gradle:9.1.0-jdk21

RUN apt-get update
RUN apt-get install -y git
RUN rm -rf /var/lib/apt/lists/*

WORKDIR /project
RUN git config --global --add safe.directory /project

CMD ["/bin/bash"]
