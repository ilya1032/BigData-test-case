FROM postgres

ADD ./limits.sql /docker-entrypoint-initdb.d