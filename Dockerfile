# Dockerfile taken depenfrom https://github.com/ToxicMushroom/Melijn
FROM openjdk:16-jdk as builder
WORKDIR /etc/helpdesk
COPY . .
USER root
# Create the shadowjar (chmod +x makes the gradlew script executable)
RUN chmod +x ./gradlew
RUN ./gradlew shadowJar

FROM openjdk:16-jdk
WORKDIR /opt/helpdesk
# Copy the shadowjar in the current workdir
COPY --from=builder ./etc/helpdesk/build/libs/ .
# Copy the .env file too
COPY --from=builder ./etc/helpdesk/.env .
# Entrypoint is used instead of CMD because the image is not intended to run another executable instead of the jar
ENTRYPOINT java \
    # Sets the java heap size (taken from .env file which is passed in the docker-compose.yml file)
    -Xmx${RAM_LIMIT} \
    # java -D tag --> set a system property
    -Dkotlin.script.classpath="/opt/helpdesk/helpdesk.jar" \
    -jar \
    ./helpdesk.jar
