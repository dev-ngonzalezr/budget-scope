FROM gradle:9.3.0-jdk25 AS development

WORKDIR /workspace/budget-scope
COPY . .
EXPOSE 8080
CMD ["gradle", "--no-daemon", ":services:api:run"]
