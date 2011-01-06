#/bin/sh


#-----------------------------------------------------------------------------
#
#	${mavenStamp}
#
#-----------------------------------------------------------------------------
# TESTED: redhat(chkconfig)
#
# The following lines are used by the 'chkconfig' init manager.
# 	They should remain commented.
#
# chkconfig:	2 3 4 5		20 80
# description:	slave-agent
#-----------------------------------------------------------------------------
# TESTED: debian(update-rc.d)
#
# The following lines are used by the LSB-compliant init managers.
# 	They should remain commented.
# 	http://refspecs.freestandards.org/LSB_3.1.0/LSB-Core-generic/LSB-Core-generic/facilname.html
#
### BEGIN INIT INFO
# Provides:          slave-agent
# Required-Start:    $local_fs $remote_fs $syslog
# Required-Stop:     $local_fs $remote_fs $syslog
# Should-Start:      $network $named $time
# Should-Stop:       $network $named $time
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: slave-agent
# Description:       slave-agent
### END INIT INFO
#-----------------------------------------------------------------------------

# use this instead of "echo"
log(){
	# one argument to echo
	echo "slave-agent: $1"
}

# Resolve the true real path and name of this script without any sym links;
REAL_PATH="$(dirname  "$(readlink -f -n $0)")"
REAL_NAME="$(basename "$(readlink -f -n $0)")"

log "REAL_PATH=$REAL_PATH"


# hudson working directory defined by script location
cd "$REAL_PATH"


# Get the real fully qualified path to this script
SCRIPT="$REAL_PATH/$REAL_NAME"

# wrapper service link:
# 	debian, redhat
WRAPPER_SVC="/etc/init.d/slave-agent"

do_install(){
    ln --symbolic --force "$SCRIPT" "$WRAPPER_SVC"
    if [ $? -eq 0 ] ; then
	log "INSTALL: added: $WRAPPER_SVC"
    else
        log "INSTALL: error: failed to add: $WRAPPER_SVC"
        exit 1
    fi
    chmod 775 "$WRAPPER_SVC"
    chown --silent --recursive "$RUN_AS_USER":root "$REAL_PATH"
}

do_uninstall(){
	rm --force "$WRAPPER_SVC"
    if [ $? -eq 0 ] ; then
	log "UNINSTALL: removed: $WRAPPER_SVC"
    else
        log "UNINSTALL: error: failed to remove: $WRAPPER_SVC"
        exit 1
    fi
}
#################################

# ensure network is available

do_ping(){
	log "network ping test"
	ping 8.8.8.8 -c 1 -i 0.2 -t 100 -W 1 && echo 1 || echo 0
}

IS_ONLINE=do_ping
MAX_CHECKS=30
CHECKS=0

while [ $IS_ONLINE -eq 0 ];do
    IS_ONLINE=do_ping
    CHECKS=$[ $CHECKS + 1 ]
    if [ $CHECKS -gt $MAX_CHECKS ]; then
        break
    fi
done

if [ $IS_ONLINE -eq 0 ]; then
	log "network is not available"
    exit 1
fi


#################################


#
#	example:
#	java -jar slave.jar -jnlpUrl https://moe.barchart.com:443/hudson/computer/linux/slave-agent.jnlp -jnlpCredentials USER:PASSWORD
#


source	THIS_PATH/slave-props.sh

log "Hudson Master : $MASTER"
log "Hudson Slave  : $SLAVE"

#################################


start(){

  status

  log "START: Hudson Slave STARTING..."

  wget -N $MASTER/jnlpJars/slave.jar

#  START="java -jar slave.jar -jnlpUrl $MASTER/computer/$SLAVE/slave-agent.jnlp"
  START="java -jar slave.jar -jnlpUrl file:///var/hudson/slave-agent.jnlp"

  nohup $START > hudson.log 2>&1 &

  log "START: Hudson Slave STARTED"

}

stop(){

  status

  log "STOP: Hudson Slave STOPPING..."

  PROC_ID=$( ps -ef | grep hudson | grep slave | grep -v grep | awk '{ print $2 }' )

  kill $PROC_ID

  log "STOP: Hudson Slave STOPPED"

}

status(){
  PROC_COUNT=$( ps aux | grep hudson | grep slave | grep -v grep | wc -l )
  if [ $PROC_COUNT -gt 0 ]; then
    log "STATUS: Hudson Slave is RUNNING"
  else
    log "STATUS: Hudson Slave is STOPPED"
  fi
}

restart(){
  stop
  start
}

# See how we were called.
case "$1" in

install)
  do_install
 ;;

uninstall)
  do_uninstall
 ;;

start)
 start
 ;;

stop)
 stop
 ;;

status)
 status
 ;;

restart)
 restart
 ;;

*)
 log $"Usage: $0 {install|uninstall|start|stop|status|restart}"
 exit 1

esac

exit 0

#/bin/sh


#-----------------------------------------------------------------------------
#
#	${mavenStamp}
#
#-----------------------------------------------------------------------------
# TESTED: redhat(chkconfig)
#
# The following lines are used by the 'chkconfig' init manager.
# 	They should remain commented.
#
# chkconfig:	2 3 4 5		20 80
# description:	slave-agent
#-----------------------------------------------------------------------------
# TESTED: debian(update-rc.d)
#
# The following lines are used by the LSB-compliant init managers.
# 	They should remain commented.
# 	http://refspecs.freestandards.org/LSB_3.1.0/LSB-Core-generic/LSB-Core-generic/facilname.html
#
### BEGIN INIT INFO
# Provides:          slave-agent
# Required-Start:    $local_fs $remote_fs $syslog
# Required-Stop:     $local_fs $remote_fs $syslog
# Should-Start:      $network $named $time
# Should-Stop:       $network $named $time
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: slave-agent
# Description:       slave-agent
### END INIT INFO
#-----------------------------------------------------------------------------

# Resolve the true real path and name of this script without any sym links;
REAL_PATH="$(dirname  "$(readlink -f -n $0)")"
REAL_NAME="$(basename "$(readlink -f -n $0)")"

# Get the real fully qualified path to this script
SCRIPT="$REAL_PATH/$REAL_NAME"

# wrapper service link:
# 	debian, redhat
WRAPPER_SVC="/etc/init.d/slave-agent"

do_install(){
    ln --symbolic --force "$SCRIPT" "$WRAPPER_SVC"
	if [ $? -eq 0 ] ; then
		log "added: $WRAPPER_SVC"
    else
        log "error: failed to add: $WRAPPER_SVC"
        exit 1
    fi
    chmod 775 "$WRAPPER_SVC"
    chown --silent --recursive "$RUN_AS_USER":root "$REAL_PATH"
}

do_uninstall(){
	rm --force "$WRAPPER_SVC"
	if [ $? -eq 0 ] ; then
		log "removed: $WRAPPER_SVC"
    else
        log "error: failed to remove: $WRAPPER_SVC"
        exit 1
    fi
}


#
#	java -jar slave.jar -jnlpUrl https://moe.barchart.com:443/hudson/computer/linux/slave-agent.jnlp
#

MASTER="https://moe.barchart.com/hudson"
echo "Hudson Master : $MASTER"

SLAVE="linux"
echo "Hudson Slave  : $SLAVE"

######################3

install(){
	do_install
}

uninstall(){
	do_uninstall
}


start(){

  wget -N $MASTER/jnlpJars/slave.jar

#  START="java -jar slave.jar -jnlpUrl $MASTER/computer/$SLAVE/slave-agent.jnlp"
  START="java -jar slave.jar -jnlpUrl file:///var/hudson/slave-agent.jnlp"

  nohup $START > hudson.log 2>&1 &

  echo "Hudson slave started"

}

stop(){

  kill `ps -ef | grep hudson | grep slave | grep -v grep | awk '{ print $2 }'`

  echo "Hudson slave stopped"

}

status(){
  numproc=`ps -ef | grep hudson | grep slave | grep -v grep | awk  | wc -l`
  if [ $numproc -gt 0 ]; then
    echo "Hudson slave is running..."
  else
    echo "Hudson slave is stopped..."
  fi
}

restart(){
  stop
  start
}

# See how we were called.
case "$1" in
start)
 start
 ;;
stop)
 stop
 ;;
status)
 status
 ;;
restart)
 restart
 ;;
*)
 echo $"Usage: $0 {start|stop|status|restart}"
 exit 1
esac

exit 0

