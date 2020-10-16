FROM azul/zulu-openjdk-alpine:15

COPY loa-generator-application-*.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]