FROM sbtscala/scala-sbt:graalvm-community-22.0.1_1.10.6_3.5.2
# For a Alpine Linux version, comment above and uncomment below:
# FROM 1science/sbt

RUN mkdir -p /app
RUN mkdir -p /app/out

WORKDIR /app

COPY . /app

CMD sbt run