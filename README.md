# discogs-rest-service

ReST service that serves Discogs dump records - artists, releases, masters and labels.

Demo:

```shell
export DB_USER=discogs; \
export DB_PASSWORD=discogs; \
export DB_URL='jdbc:postgresql://localhost:5432/discogs'; \
./gradlew run --args='8080'
```

## Setup

Insert Discogs dump to PostgreSQL database: https://github.com/tslic/discogs-dump2db

Publish this package to the local maven repo: https://github.com/tslic/discogs-jooq
