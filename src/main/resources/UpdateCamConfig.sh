#!/bin/bash

Usage=$(cat <<EOF
Usage: $(basename $0) -f from_file [ -t to_file ] [ -p prefix ] [ -b base_dir ]
This command will help to merge properties file (from_file) into properties file (to_file)
Properties in from_file will be taken if there is any conflict
If -p option is specified, only properties starting with the prefix in from_file will be merged. And prefix will be removed.
EOF
)

Prefix=''
BaseDir=''
ToFile=''
FromFile=''

while getopts f:t:p:b: op
do  
    case "$op" in
    f)  FromFile="$OPTARG"
        ;;
    t)  ToFile="$OPTARG"
        ;;
    p)  Prefix="$OPTARG"
        ;;
    b)  BaseDir="$OPTARG"
        ;;
    ?)  echo "$Usage"
        exit 1
        ;;
    esac
done

BaseDir=${BaseDir:-/opt/vmware/hms/conf}
ToFile=${ToFile:-$BaseDir/cam-config.properties}

if [ -z "$FromFile" ]; then
    echo "$Usage"
    exit 1
fi

if [ ! -f "$FromFile" ]; then
    echo "Template file $FromFile does not exist"
    exit 1
fi

if [ ! -f "$ToFile" ]; then
    echo "$ToFile does not exist"
    exit 1
fi

cat $FromFile | grep -v "^\s*$" | grep -v "^#" | grep "^$Prefix" | while read line;
do
    #echo $line
    # Non-greedy matcher to pick the property name
    PropName=$(echo "$line" | sed 's/^\([^=]*\)=.*/\1/')
    if [ -n "$Prefix" ]; then
        PropName=${PropName##$Prefix.}
    fi

    PropValue=$(echo "$line" | sed 's/^[^=]*=\(.*\)/\1/')
    echo "Updating property [$PropName] with value [$PropValue]"

    Filelink=$(expr "$PropValue" : '{file:\(.*\)}' | grep -v ^#)

    if [ -n "$Filelink" ]; then
        RefFilename=$BaseDir/$(echo "$Filelink" | awk 'BEGIN{FS="#"}{print $1}')
        RefPropName=$(echo "$Filelink" | awk 'BEGIN{FS="#"}{print $2}')
        PropValue=$(cat $RefFilename | grep ^$RefPropName= | sed 's/^[^=]*=\(.*\)/\1/')
        echo "This value is a reference, find property [$RefPropName] in reference file $RefFilename, value is [$PropValue]"
    fi

    Shelllink=$(expr "$PropValue" : '{script:\(.*\)}' | grep -v ^#)
    if [ -n "$Shelllink" ]; then
        PropValue=$(eval "$Shelllink" | xargs)
        echo "This value is a shell script [$Shelllink], calculate property [$RefPropName], value is [$PropValue]"
    fi

    if [ -n "$(grep ^$PropName= $ToFile)" ]; then
        sed -i'' "s|^$PropName=.*|$PropName=$PropValue|" $ToFile
        echo "Property $PropName has been updated"
    else
        echo "$PropName=$PropValue" >> $ToFile
        echo "Property $PropName has been created"
    fi
done

echo "success"
exit 0