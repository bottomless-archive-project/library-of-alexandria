FROM azul/zulu-openjdk-alpine:14

COPY loa-indexer-application-*.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]