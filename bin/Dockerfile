# base openjdk
FROM selenium/standalone-chrome:3.141.59-dubnium

ENV TZ Asia/Seoul

RUN sudo useradd --system scrapper

ADD batch-scrapper-1.0-SNAPSHOT.jar /home/scrapper/
ADD start_yna_stream_app.sh /home/scrapper/
ADD webdriver /home/scrapper/webdriver

RUN sudo chown -R scrapper:scrapper /home/scrapper

USER scrapper

WORKDIR /home/scrapper
CMD ["/home/scrapper/start_yna_stream_app.sh"]
