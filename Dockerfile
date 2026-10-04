FROM ubuntu:latest
LABEL authors="lacas"

ENTRYPOINT ["top", "-b"]