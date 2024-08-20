FROM azul/zulu-openjdk-debian:21-jre-headless

# Set Timezone to Europe/Berlin
RUN apt-get install -y tzdata
ENV TZ="Europe/Berlin"

# Create app directory
RUN mkdir /opt/release-tracker
WORKDIR /opt/release-tracker

# Add jar
ADD target/release-tracker-*-jar-with-dependencies.jar release-tracker.jar

ENTRYPOINT ["java", "-jar", "-XX:MaxRAMPercentage=90", "/opt/release-tracker/release-tracker.jar"]
