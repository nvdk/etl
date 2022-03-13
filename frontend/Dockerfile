FROM node:16.14.0-bullseye as lp-etl-build
RUN apt-get update \
 && apt-get -y --no-install-recommends install openjdk-17-jdk
WORKDIR /opt/lp-etl
VOLUME ["/root/.m2/repository"]
COPY ./ ./
RUN ./mvnw install $LP_ETL_BUILD_JAVA_TEST -P "install-frontend"

FROM node:16.14.0-bullseye
ARG LP_ETL_USER=5987
ENV LP_ETL_FRONTEND_CONFIGURATION=""
RUN addgroup --gid $LP_ETL_USER "linkedpipes" \
    && useradd -g "linkedpipes" -u $LP_ETL_USER "etl"
WORKDIR /opt/lp-etl/frontend
COPY --from=lp-etl-build --chown=$LP_ETL_USER:$LP_ETL_USER /opt/lp-etl/deploy/frontend ./
USER $LP_ETL_USER
EXPOSE 8080
CMD ["npm", "run", "start"]
