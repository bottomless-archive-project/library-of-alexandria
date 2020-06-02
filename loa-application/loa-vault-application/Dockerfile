FROM azul/zulu-openjdk-alpine:14-jre-headless

COPY loa-vault-application-*.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]