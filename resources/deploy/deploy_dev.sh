#!/bin/sh
cd /home/dev/bin/javalib/
echo copying ngat_new_oss.war to root@ltdev2:/usr/local/tomcat/webapps
scp ngat_new_oss.war root@ltdev2:/usr/local/tomcat/webapps

#echo copying ngat_new_phase2.jar to eng@ltdev1:/occ/common/experimental
echo .jar files to eng@ltdev1:/occ/common/experimental
scp ngat_new_phase2.jar ngat_new_oss.jar ngat_new_oss_client.jar eng@ltdev1:/occ/common/experimental
#scp ngat_new_oss.war ngat_new_phase2.jar ngat_new_oss.jar ngat_new_oss_client.jar eng@ltproxy:download

echo RESTART SERVICES and COPY .jars TO CLIENT
