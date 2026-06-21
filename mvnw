#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# MAVEN_SKIP_RC - flag to disable loading of mavenrc files
if [ -z "$MAVEN_SKIP_RC" ] ; then

  if [ -f /usr/local/etc/mavenrc ] ; then
    . /usr/local/etc/mavenrc
  fi

  if [ -f /etc/mavenrc ] ; then
    . /etc/mavenrc
  fi

  if [ -f "$HOME/.mavenrc" ] ; then
    . "$HOME/.mavenrc"
  fi

fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true;;
  Darwin*) darwin=true
    # Use /usr/libexec/java_home if available, otherwise fall back to /Library/Java/Home
    if [ -z "$JAVA_HOME" ]; then
      if [ -x "/usr/libexec/java_home" ]; then
        export JAVA_HOME="`/usr/libexec/java_home`"
      else
        export JAVA_HOME="/Library/Java/Home"
      fi
    fi
    ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -x /usr/libexec/java_home ] ; then
    export JAVA_HOME="`/usr/libexec/java_home`"
  elif [ -d /usr/1.8.0_* ] ; then
    export JAVA_HOME=/usr/1.8.0_*
  elif [ -d /usr/1.7.0_* ] ; then
    export JAVA_HOME=/usr/1.7.0_*
  fi
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Error: JAVA_HOME is not set and cannot be found." >&2
  exit 1
fi

CLASSWORLDS_LAUNCHER=org.codehaus.plexus.classworlds.launcher.Launcher

# traverses directory structure from process work directory to filesystem root
# first directory with .mvn subdirectory is considered project base directory
find_maven_basedir() {

  if [ -z "$1" ]
  then
    echo "Path not specified to find_maven_basedir"
    return 1
  fi

  basedir="$1"
  wdir="$1"
  while [ "$wdir" != '/' ] ; do
    if [ -d "$wdir"/.mvn ] ; then
      basedir=$wdir
      break
    fi
    # workaround for JBEAP-8937 (on Solaris 10/Sparc)
    if [ -d "${wdir}" ]; then
      wdir=`cd "$wdir/.."; pwd`
    fi
    # end of workaround
  done
  echo "${basedir}"
}

# concatenates all lines of a file
concat_lines() {
  if [ -f "$1" ]
  then
    echo "$(tr -s '\n' ' ' < "$1")"
  fi
}

BASE_DIR=`find_maven_basedir "$(pwd)"`
if [ -z "$BASE_DIR" ] ; then
  exit 1
fi

##########################################################################################
# Extension to allow automatically downloading the maven-wrapper.jar from Maven-central
# This allows using the maven wrapper in projects that prohibit checking in binary data.
##########################################################################################
if [ -r "$BASE_DIR/.mvn/wrapper/maven-wrapper.jar" ] ; then
    if [ "$MVNW_VERBOSE" = true ]; then
      echo "Found .mvn/wrapper/maven-wrapper.jar"
    fi
    MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$BASE_DIR"}
    MAVEN_SCRIPT="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.sh"
    MAVEN_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
    if [ -n "$MAVEN_CUSTOM_OPTS" ]; then
      MAVEN_OPTS="$MAVEN_OPTS $MAVEN_CUSTOM_OPTS"
    fi
    # For Cygwin, switch paths to Windows format before running java
    if $cygwin; then
      [ -n "$M2_HOME" ] &&
        M2_HOME=`cygpath --path --windows "$M2_HOME"`
      [ -n "$MAVEN_PROJECTBASEDIR" ] &&
        MAVEN_PROJECTBASEDIR=`cygpath --path --windows "$MAVEN_PROJECTBASEDIR"`
      [ -n "$CLASSPATH" ] &&
        CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
      [ -n "$MAVEN_CLASSPATH_CONTAINER" ] &&
        MAVEN_CLASSPATH_CONTAINER=`cygpath --path --windows "$MAVEN_CLASSPATH_CONTAINER"`
      if [ "x$MAVEN_OPTS" != x ] ; then
        MAVEN_OPTS=`echo "$MAVEN_OPTS" | sed 's/ -D/ -Dprop:/g'`
      fi
    fi
    sh "$MAVEN_SCRIPT"
    exit $?
else
    if [ "$MVNW_VERBOSE" = true ]; then
      echo "Falling back to M2_HOME to find maven"
    fi
    if [ -n "$M2_HOME" ]; then
      MAVEN_SCRIPT="$M2_HOME/bin/mvn"
    else
      MAVEN_SCRIPT="`which mvn`"
    fi
    if [ ! -x "$MAVEN_SCRIPT" ]; then
      echo "Error: Maven script not found. Please check M2_HOME env var and mvn in PATH" >&2
      exit 1
    fi
    "$MAVEN_SCRIPT" "$@"
fi

