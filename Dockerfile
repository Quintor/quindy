FROM maven:3.5-jdk-8
ADD pom.xml /
ADD student/pom.xml /student/pom.xml
ADD university/pom.xml /university/pom.xml
ADD indy-wrapper/pom.xml /indy-wrapper/pom.xml
ADD integration-tests/pom.xml /integration-tests/pom.xml
RUN mvn package
ADD . /
RUN mvn clean install
