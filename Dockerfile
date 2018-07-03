FROM centos:7 as build
MAINTAINER Verisign Labs

RUN mkdir -p /app/rdap-odf

COPY pom.xml.docker /app/rdap-odf/pom.xml

RUN yum install -y java-1.8.0-openjdk-devel maven

COPY src/ /app/rdap-odf/src/

WORKDIR /app/rdap-odf

RUN mvn clean install -DskipTests

FROM centos:7

RUN mkdir -p /app/rdap-odf

RUN yum install -y java-1.8.0-openjdk; yum clean all; rm -rf /var/cache/yum

COPY --from=build /app/rdap-odf/target/rdap-odf-1.0.jar /app/rdap-odf/rdap-odf-1.0.jar 

COPY testScript.txt /app/rdap-odf/testScript.txt 

WORKDIR /app/rdap-odf

ENTRYPOINT ["java"]
CMD ["-jar", "rdap-odf-1.0.jar"]




