FROM azul/zulu-openjdk-alpine:14

COPY loa-web-application-*.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]