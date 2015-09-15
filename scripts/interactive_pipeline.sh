DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

realpath /usr/bin/java

# load jdk, etc...
#source ~/.bashrc

# Ensure jdk is in the environmnent variable PATH
#if [ ! "$(echo $PATH | grep "jdk" )" ]; then
#	echo "No jdk found... Adding default jdk (/tools/jdk1.7.0_21/bin) to your PATH environment variable"
#	# Add jdk location to the PATH environment variable
#	PATH="/tools/jdk1.7.0_21/bin:${PATH}"
#fi

# Add TextPro to the LD_LIBRARY_PATH
#export LD_LIBRARY_PATH="/mnt/sdb/shared/mylibs:."
#echo "LD_LIBRARY_PATH=${LD_LIBRARY_PATH}"

# load the classpath
source $DIR/../classpath.txt

echo "CLASSPATH: $CLASSPATH"

echo "DIR: $DIR"

# load settings from a config file
function load {
	source "$1"
}

#echo "len(args): $#"

# check that the right args numbre is passaed, thus
# check that settings file is specified
if [ $# != 1 ]; then
echo "wrong arguments number: $#"
# print usage
echo "usage: ./interactive_pipeline <settings_file>"
exit 1
fi
echo "args: $@"

# Asssign first argument to var config_file
settings_file="${1:?\"settings file not specified\"}"

echo -n "loading settings from file \"$settings_file..."

# check that the settings file does exist
if [ ! -f "$settings_file" ]; then
echo "failed (\"${settings_file}\" does not exist or is not a file)"
exit 1
fi
# load settings
load "$settings_file"
echo "done"


echo "lang = \"$lang\""
echo "index = \"$index\""
echo "modelFile = \"$modelFile\""
echo "candidatesNumber = \"$candidatesNumber\""

# check that lang is neither null nor empty
if [ -z $lang ] || [ ! "$lang" ]; then
	echo "lang not specified"
exit 1
fi

# check that index is neither null nor empty	
if [ -z "$index" ] || [ ! "$index" ]; then
echo "index not specified"
exit 1
fi

# check taht modelFile is neither null nor empty
if [ -z "$modelFile" ] || [ ! "$modelFile" ]; then
echo "modelFile not specified"
exit 1
fi

if [ -z "$candidatesNumber" ] || [ ! "$candidatesNumber" ]; then
echo "candidatesNumber not specified"
exit 1
fi


	echo "lang = \"$lang\""
	echo "index = \"$index\""
	echo "modelFile = \"$modelFile\""

	# check that lang is neither null nor empty
	if [ -z $lang ] || [ ! "$lang" ]; then
		echo "lang not specified"
		exit 1
	fi

	# check that index is neither null nor empty	
	if [ -z "$index" ] || [ ! "$index" ]; then
		echo "index not specified"
		exit 1
	fi
	
	# check taht modelFile is neither null nor empty
	if [ -z "$modelFile" ] || [ ! "$modelFile" ]; then
		echo "modelFile not specified"
		exit 1
	fi

	cmd="java -Xss128m -Dlog4j.debug"
	# check that log4j configuration file is specified
	if [ ! -z "$log4jConfigurationFile" ] && [ "$log4jConfigurationFile" ]; then
		# Add log4j configuration file 
		echo "log4jConfigurationFile = \"$log4jConfigurationFile\""
		cmd="$cmd -Dlog4j.configuration=file:$log4jConfigurationFile"
	fi
	cmd="$cmd -cp $CLASSPATH qa.qcri.qf.pipeline.qademo.InteractivePipeline"
	cmd="$cmd -lang $lang"
	cmd="$cmd -index \"$index\""
	cmd="$cmd -modelFile \"$modelFile\""

	# if the option candidatesNumber is defined, add it to cmd
	if [ ! -z "$candidatesNumber" ] && [ "$candidatesNumber" ]; then
		cmd="$cmd -candidatesNumber \"$candidatesNumber\""
	fi

	if [ ! -z "$filterDuplicates" ] && [ "$filterDuplicates" ]; then
		cmd="$cmd -filterDuplicates $filterDuplicates"
	fi

	if [ ! -z "$maxCandidatesNumber" ] && [ "$maxCandidatesNumber" ]; then
		cmd="$cmd -maxCandidatesNumber $maxCandidatesNumber"
	fi
 
        echo "run: $cmd"
	eval "$cmd"



<<COMMENT
# print the value of var
function print { 
	var_name="${1:?"var name not specified"}"
		echo "$var_name = \"${!var_name}\""
}


function run {
	echo "lang = \"$lang\""
	echo "index = \"$index\""
	echo "modelFile = \"$modelFile\""
	echo "candidatesNumber = \"$candidatesNumber\""

	# check that lang is neither null nor empty
	if [ -z $lang ] || [ ! "$lang" ]; then
		echo "lang not specified"
		exit 1
	fi

	# check that index is neither null nor empty	
	if [ -z "$index" ] || [ ! "$index" ]; then
		echo "index not specified"
		exit 1
	fi
	
	# check taht modelFile is neither null nor empty
	if [ -z "$modelFile" ] || [ ! "$modelFile" ]; then
		echo "modelFile not specified"
		exit 1
	fi

	if [ -z "$candidatesNumber" ] || [ ! "$candidatesNumber" ]; then
		echo "candidatesNumber not specified"
		exit 1
	fi

	cmd="java -Xss128m -cp $CLASSPATH qa.qcri.qf.pipeline.qademo.InteractivePipeline"
	cmd="$cmd -lang $lang"
	cmd="$cmd -index \"$index\""
	cmd="$cmd -modelFile \"$modelFile\""

	# if the option candidatesNumber is defined, add it to cmd
	if [ ! -z "$candidatesNumber" ] && [ "$candidatesNumber" ]; then
		cmd="$cmd -candidatesNumber \"$candidatesNumber\""
	fi

	if [ ! -z "$printExamples" ] && [ "$printExamples" ]; then
		cmd="$cmd -printExamples $printExamples"
	fi

	echo "cmd: $cmd"
	eval "$cmd"
}


while [ -n "$(echo $1 | grep '-')" ]; do
	case $1 in 
		-load) echo '-process options -load'
			load "$2"
			shift;;
		-print) echo '-process option -print'
			print "$2"
			shift;;
		-run) echo '-process option -run'
			run
			shift;;
	esac
	shift
done
COMMENT

