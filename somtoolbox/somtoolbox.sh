#!/bin/bash
set -e

MINWIDTH=140
MINHEIGHT=30

# Resize Terminal if to small
if [ "$TERM" == "xterm" ]; then
    if [[ $(tput lines) -lt $MINHEIGHT ]] && [[ $(tput cols) -lt $MINWIDTH ]]; then
	#echo "Resizing window"
	echo -en "\e[8;$MINHEIGHT;$MINWIDTH;t"
    elif [[ $(tput lines) -lt $MINHEIGHT ]]; then
	#echo "Resizing window"
	echo -en "\e[8;$MINHEIGHT;$(tput cols);t"
    elif [[ $(tput cols) -lt $MINWIDTH ]]; then
	#echo "Resizing window"
	echo -en "\e[8;$(tput lines);$MINWIDTH;t"
    fi
fi

# Save working directory
WD=$(pwd)

# Resolve symbolic links (with -f even multiple!)to get real path of program
REALPATH=$(readlink -f $0 2>/dev/null || true)

# Save the name of the programm
PROG=$(basename $0)

# Get the basedir of the programm
BASE_DIR=$(dirname $REALPATH 2>/dev/null || true)
# readlink can cause problems on OSX, therefor, as fallback...
BASE_DIR=${BASE_DIR:-"%{INSTALL_PATH}"}


# Check for a somtoolboxrc-file and use it:
if [ -r $BASE_DIR/somtoolboxrc ]; then
    source $BASE_DIR/somtoolboxrc
fi

# Check for a user-somtoolboxrc:
mkdir -p ~/.somtoolbox/
if [ -r ~/.somtoolbox/somtoolboxrc ]; then
    source ~/.somtoolbox/somtoolboxrc  
fi

# If JAVA is not defined yet, we default to "java"
JAVA=${JAVA:-"java"}

# If JAVA_OPTS is not defined yet, we default to
if [ -z "$JAVA_OPTS" ]; then
    JAVA_VERSION=$($JAVA -version 2>&1| tail -n 1)
    # Java Options: give a lot of memory on 64 bit JVMs, 
    #   and the max allowed on 32 bit
    if [ -e /proc/meminfo ]; then
	totalMemory="`cat /proc/meminfo | grep MemTotal | cut -d: -f2 | cut -dk -f1`"
    else
	totalMemory=$((2 * 1024 * 1024))
	cat <<EOF
Cant determine available memory.

Please define an apropriate value in
 ~/.somtoolbox/somtoolboxrc
by setting
 JAVA_OPTS="-server -Xmx[RRR]M"
replacing [RRR] with the available memory (in MB)

Now trying to continue with some guessed default value...
EOF
    fi

    if [[ $JAVA_VERSION == *64-Bit* ]]; then
    	# use all memory but 1 GB
	usedMemory=$(( ( totalMemory / 1024 ) - 1024))
	JAVA_OPTS="-server -Xmx${usedMemory}M"
    else
    	# use 80% of memory, but max 3600M
    	usedMemory=$(( ( ( totalMemory / 1024 ) / 10 ) * 8 ))
    	if [ "$usedMemory" -gt "3600" ]; then
    	    usedMemory="3600"
	fi 
	JAVA_OPTS="-server -Xmx${usedMemory}M"
    fi
fi

# Check for the executable
if [ "$PROG" != "$(basename $REALPATH 2>/dev/null || echo $PROG)" ]; then
    MAIN_CLASS=$PROG
else
    MAIN_CLASS=$1
    shift || true
fi

## SET UP
# Libraries
LIB_DIR=$BASE_DIR/lib

# we prefere the jar-file
if [ -e $BASE_DIR/somtoolbox.jar ]
then 
    CP=$BASE_DIR/somtoolbox.jar
    USING=somtoolbox.jar
else 
    CP=$BASE_DIR/bin/core/
    USING=class-files
fi

# add optional components
if [ ! -e $LIB_DIR/somtoolbox+opt.jar ]; then
    if [ -e $BASE_DIR/bin/optional/ ]; then
        CP=$CP:$BASE_DIR/bin/optional/
    fi
fi

# add external jars to classpath
for i in $(find $LIB_DIR/ -name '*.jar'); do
    CP=$CP:$i
done

# add resources
CP=$CP:$BASE_DIR/rsc

# Check for simple commands
QUIET=false
if [ "$#" -gt "0" ]; then
    for p in $@; do
	case $p in
	    --help)
		QUIET=true
		;;
	    --version)
		QUIET=true
		;;
	    --list-mains)
		MAIN_CLASS=$p
		QUIET=true
		;;
	esac
    done
else
    QUIET=true
fi

# Print the environment
if [ "$QUIET" != "true" ]; then
    cat <<EOF
+-----------------------------------------------------------------------
|
| $PROG environment:
|  - using $USING
|
| java:               $JAVA
|
| java options:       $JAVA_OPTS
|
| working directory:  $WD
|
| base directory:     $BASE_DIR
|
| lib directory:      $LIB_DIR
|
+-----------------------------------------------------------------------
EOF
#classpath:            $CP
fi

export LINES=$(tput lines)
export COLUMNS=$(tput cols)
export SOMTOOLBOX_BASEDIR="$BASE_DIR"

$JAVA $JAVA_OPTS -cp $CP at.tuwien.ifs.somtoolbox.apps.SOMToolboxMain $MAIN_CLASS "$@"
