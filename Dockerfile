FROM hseeberger/scala-sbt

################################# end

RUN mkdir -p /opt/zipstreaming
WORKDIR /opt/zipstreaming

EXPOSE 8087

COPY . /opt/zipstreaming/

RUN sbt assembly

CMD ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "/opt/zipstreaming/target/scala-2.12/zip-streaming.jar"]
