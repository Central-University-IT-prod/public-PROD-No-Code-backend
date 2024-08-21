FROM gradle:latest AS builder

COPY . /workspace/app

WORKDIR /workspace/app

RUN gradle build --no-daemon || return 0
RUN gradle build --no-daemon

FROM openjdk:latest
COPY --from=builder /workspace/app/build/libs/*.jar /app/backend.jar

CMD ["java", "-jar", "/app/backend.jar"]