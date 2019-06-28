FROM studybits/base-image
RUN mkdir /quindy
WORKDIR /quindy
ADD pom.xml /quindy/
RUN mvn package
ADD . /quindy/
CMD sh ./test.sh
