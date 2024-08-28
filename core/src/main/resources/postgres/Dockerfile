FROM postgres:16
ENV POSTGRES_DB monet_test
ENV POSTGRES_USER postgres
ENV POSTGRES_PASSWORD changeme
ADD tables.sql /docker-entrypoint-initdb.d/