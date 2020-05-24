FROM azul/zulu-openjdk-alpine:13-jre-headless

COPY loa-web-application-*.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]