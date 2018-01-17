#!/bin/bash
#Script to shutdown the instance when doesn't have slurm jobs
#@Author igomez, mnunez
#@Date 11-8-2016

proc=`ps -eaf | grep -v grep |  grep -c slurmstepd`
sleep=30
max_failed_checks=30

#echo $proc


failed_checks=0

while :
do
   echo "checking number of jobs"
   echo $proc
    sleep $sleep
    if  [ $proc ==  0 ]; then
        failed_checks=$(( failed_checks+1 ))
    else
        failed_checks=0
    fi
    echo "number of failed checks = $failed_checks"
    if [ $failed_checks == $max_failed_checks ]; then
        echo "shutting down"
        shutdown -h now
    fi
    proc=`ps -eaf | grep -v grep |  grep -c slurmstepd`
done
