FROM maven:3.5-jdk-8
ADD pom.xml /
ADD studybits-student/pom.xml /studybits-student/pom.xml
ADD studybits-university/pom.xml /studybits-university/pom.xml
ADD indy-wrapper/pom.xml /indy-wrapper/pom.xml
RUN mvn package
ADD . /
RUN mvn install
