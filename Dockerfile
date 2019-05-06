FROM studybits/base-image

ADD pom.xml /
RUN mvn package
ADD . /
CMD sh -e -c "if $DEPLOY; then mvn clean package verify deploy; else mvn verify; fi"
