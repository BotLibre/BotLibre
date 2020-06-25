#!/usr/bin/sh
cp -nv ../default-conf/* ../default-conf/.keystore ../conf/
exec $@
