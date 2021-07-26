FROM alpine as java-builder
WORKDIR /root/

RUN apk update
RUN apk add --no-cache ca-certificates wget

RUN wget --no-cookies --no-check-certificate \
		--header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" \
		"http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jdk-8u131-linux-x64.tar.gz" && \
	tar xzf jdk-8u131-linux-x64.tar.gz && \
	rm jdk-8u131-linux-x64.tar.gz


FROM alpine as tomcat-builder
WORKDIR /root/

RUN TOMCAT_VERSION=$(wget -q -O - https://downloads.apache.org/tomcat/tomcat-8/ | grep v8 | grep -oE "8\.5\.[0-9]+" | head -n 1) && \
	wget "https://downloads.apache.org/tomcat/tomcat-8/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz" && \
	tar xzf apache-tomcat-8.5.*.tar.gz && \
	rm apache-tomcat-8.5.*.tar.gz && \
	mv apache-tomcat-8.5.*/ tomcat/
# RUN wget https://www-us.apache.org/dist/tomcat/tomcat-8/v8.5.56/bin/apache-tomcat-8.5.56.tar.gz && \
# 	tar xzf apache-tomcat-8.5.56.tar.gz && \
# 	rm apache-tomcat-8.5.56.tar.gz && \
# 	mv apache-tomcat-8.5.56/ tomcat/
RUN rm -rf tomcat/webapps/*
RUN mv tomcat/conf/ tomcat/default-conf/


FROM alpine as botlibre-files-builder
WORKDIR /root/

RUN wget https://www.botlibre.biz/download/voices52.zip && \
	unzip voices52.zip && \
	rm voices52.zip

RUN wget https://www.botlibre.biz/download/lib.zip  && \
	mkdir lib/ && \
	unzip lib.zip -d lib/ && \
	rm lib.zip

FROM centos:8
RUN dnf update -y && dnf -y clean all

COPY --from=java-builder /root/jdk1.8.0_131 /opt/jdk1.8.0_131
RUN alternatives --install /usr/bin/java java /opt/jdk1.8.0_131/bin/java 2 && \
	export JAVA_HOME=/opt/jdk1.8.0_131

COPY --from=tomcat-builder /root/tomcat /usr/local/tomcat8/
VOLUME /usr/local/tomcat8/conf/

COPY --from=botlibre-files-builder /root/voices/ /usr/local/tomcat8/bin/lib/voices/
COPY --from=botlibre-files-builder /root/lib/ /usr/local/tomcat8/lib/

COPY setup-conf.sh /usr/local/tomcat8/bin/setup-conf.sh
RUN [ "chmod", "750", "/usr/local/tomcat8/bin/setup-conf.sh" ]
COPY conf/* /usr/local/tomcat8/default-conf/
COPY *.war /usr/local/tomcat8/webapps/

EXPOSE 80 443 25 5901-5902 465 110 995 143 993 6665

WORKDIR /usr/local/tomcat8/bin
ENTRYPOINT [ "./setup-conf.sh", "./catalina.sh", "run" ]
