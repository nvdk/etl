FROM ubuntu:jammy-20220301 as lp-etl-build
RUN apt-get update \
 && apt-get -y --no-install-recommends install openjdk-17-jdk
WORKDIR /opt/lp-etl
VOLUME ["/root/.m2/repository"]
COPY ./ ./
RUN ./mvnw install $LP_ETL_BUILD_JAVA_TEST -P "install-executor,install-executor-monitor,install-storage,install-plugins"

FROM ubuntu:jammy-20220301
ARG LP_ETL_USER=5987
RUN apt-get update \
 && apt-get -y --no-install-recommends install openjdk-17-jdk
RUN addgroup --gid $LP_ETL_USER "linkedpipes" \
    && useradd -g "linkedpipes" -u $LP_ETL_USER "etl"
RUN mkdir -p /data/lp-etl/storage \
    && chown $LP_ETL_USER:$LP_ETL_USER /data/lp-etl/storage
WORKDIR /opt/lp-etl/
COPY --from=lp-etl-build --chown=$LP_ETL_USER:$LP_ETL_USER /opt/lp-etl/deploy/storage ./storage
COPY --from=lp-etl-build --chown=$LP_ETL_USER:$LP_ETL_USER /opt/lp-etl/deploy/jars ./components
USER $LP_ETL_USER
EXPOSE 8080
EXPOSE 8083
CMD ["java", "-jar" ,"./storage/storage.jar", "start"]