<a href="url"><img src="ziptrack_logo.png" align="right" width="200" ></a>

# ZipTrack

## Description
ZipTrack analyses traces of concurrent programs, compressed as SLPs (straight line programs)
and checks if there is a race.
ZipTrack performs two analyses :

	1. HB race detection
	2. LockSet violation detection

ZipTrack is written in Java. 
The following classes let you perform different analyses:

	1. `ZipHB.java` - for HB race detection on compressed traces
	2. `ZipLockSet.java` - for detecting violations of lockset discipline on compressed traces
	3. `ZipMetaInfo.java` - for printing trace characteristics.
	4. `TransformGrammar.java` - for transforming an SLP S into another SLP S' with more production rules that have terminal symbols only.
	5. `PrintTrace.java` - Convert an execution trace generated by RVPredict into a readable format.  
	**Update** - ZipTrack now supports two other formats - [STD](https://github.com/umangm/rapid/) and [RoadRunner's trace format](https://github.com/umangm/rapid/blob/master/notes/Generate_RoadRunner_traces.md).

## Usage

In order to use ZipTrack, you need traces.
We use [RVPredict](https://runtimeverification.com/predict/)'s logger functionality for this.  
We then compress these traces as SLPs, using [Sequitur](https://github.com/craignm/sequitur).
These compressed traces can then be analyzed by ZipTrack.

**Updates** -
- RVPredict's support is deprecated. If still interested, you should use [this](https://uofi.box.com/v/rvpredict) version of RVPredict for logging traces. If this version does not work, your best bet is to download the latest version from [RVPredict's website](https://runtimeverification.com/predict/).
- Recently, we also added support for using [RoadRunner](https://github.com/stephenfreund/RoadRunner)'s logging functionality. See [here](https://github.com/umangm/rapid/blob/master/notes/Generate_RoadRunner_traces.md) for help on how to generate RoadRunner logs.
- We have also added support for manually written traces in the STD (Standard) format described [here](https://github.com/umangm/rapid/). This is a simple format and one can handcraft traces in this format for testing purposes.




### Generating traces:

1. ~~Download and install [RVPredict](https://runtimeverification.com/predict/).~~
Download [this](https://uofi.box.com/v/rvpredict) version of RVPredict and if it does not work, use [the latest version from the website](https://runtimeverification.com/predict/).
2. Run the logger :

```
java -jar /path/to/rv-predict.jar  --log  --base-log-dir /path/to/base_folder --log-dirname sub_folder <java_class_to_be_analyzed>

```
This command creates binary log files in `/path/to/base_folder/sub_folder`.

**Update** - You can now generate traces using [RoadRunner](https://github.com/stephenfreund/RoadRunner). See [here](https://github.com/umangm/rapid/blob/master/notes/Generate_RoadRunner_traces.md) for instructions.  
Additionally, you can also write your own traces in the more readable STD format described [here](https://github.com/umangm/rapid/).

### Compiling ZipTrack

Use the build file :

```
cd /path/to/ziptrack
ant jar
```


### Compressing traces:

1. First convert the bin files into a readable format. 
- When using RVPRedict traces, run the following command:
```
java -classpath /path/to/ziptrack/lib/*:/path/to/ziptrack/ziptrack.jar PrintTrace -p=/path/to/base_folder/sub_folder -f=rv -m=/path/to/base_folder/sub_folder/map.txt > /path/to/base_folder/sub_folder/trace.txt 
```
This command creates two files: `/path/to/base_folder/sub_folder/trace.txt` and `/path/to/base_folder/sub_folder/map.shared.txt`

- When using RoadRunner traces, run the following command:
```
java -classpath /path/to/ziptrack/lib/*:/path/to/ziptrack/ziptrack.jar PrintTrace -p=/path/to/input_trace_file.rr -f=rr -m=/path/to/map.txt > /path/to/trace.txt 
```
This command creates two files: `/path/to/trace.txt` and `/path/to/map.txt`

- When using traces in STD format, run the following command:
```
java -classpath /path/to/ziptrack/lib/*:/path/to/ziptrack/ziptrack.jar PrintTrace -p=/path/to/input_trace_file.std -f=std -m=/path/to/map.txt > /path/to/trace.txt 
```
This command creates two files: `/path/to/trace.txt` and `/path/to/map.txt`



2. Use Sequitur to compress `trace.txt`. 
For this, first compile Sequitur.
ZipTrack comes with a copy of Sequitur (forked from [here](https://github.com/craignm/sequitur/)).
```
cd /path/to/ziptrack/sequitur/c++
make
/path/to/ziptrack/sequitur/c++/sequitur -d -p -m 2000 < /path/to/base_folder/sub_folder/trace.txt > /path/to/base_folder/sub_folder/slp.txt
```

This command creates the SLP in the file `/path/to/base_folder/sub_folder/slp.txt`.

### Run ZipTrack

1. [Optional] First transform the grammar:
```
java -Xmx10000m -Xms10000m -classpath /path/to/ziptrack/ziptrack.jar:/path/to/ziptrack/lib/* TransformGrammar -m /path/to/base_folder/sub_folder/map.shared.txt -t /path/to/base_folder/sub_folder/slp.txt  -s > /path/to/base_folder/sub_folder/slp_new.txt
## Replace the old SLP :
cp /path/to/base_folder/sub_folder/slp.txt /path/to/base_folder/sub_folder/slp_old.txt
cp /path/to/base_folder/sub_folder/slp_new.txt /path/to/base_folder/sub_folder/slp.txt 
```

2. Run ZipHB :

```
java -Xmx10000m -Xms10000m -classpath /path/to/ziptrack/ziptrack.jar:/path/to/ziptrack/lib/* ZipHB -m /path/to/base_folder/sub_folder/map.shared.txt -t /path/to/base_folder/sub_folder/slp.txt  -s 
```

3. Run ZipLockSet :

```
java -Xmx10000m -Xms10000m -classpath /path/to/ziptrack/ziptrack.jar:/path/to/ziptrack/lib/* ZipLockSet -m /path/to/base_folder/sub_folder/map.shared.txt -t /path/to/base_folder/sub_folder/slp.txt  -s 
```

4. Run ZipMetaInfo :

```
java -Xmx10000m -Xms10000m -classpath /path/to/ziptrack/ziptrack.jar:/path/to/ziptrack/lib/* ZipMetaInfo -m /path/to/base_folder/sub_folder/map.shared.txt -t /path/to/base_folder/sub_folder/slp.txt
```


### Run analyses on Uncompressed traces

We will use [RAPID](https://github.com/umangm/rapid/) for this.

First, clone RAPID and run `ant jar` in RAPID's directory.
Then, follow these steps:

1. Run Djit+ VC algorithm :

```
java -Xmx10000m -Xms10000m -classpath /path/to/rapid/rapid.jar:/path/to/rapid/lib/* HB -p=/path/to/base_folder/sub_folder -f=rv -s
```

2. Run FastTrack VC algorithm :

```
java -Xmx10000m -Xms10000m -classpath /path/to/rapid/rapid.jar:/path/to/rapid/lib/* HBEpoch -p=/path/to/base_folder/sub_folder -f=rv -s
```

3. Run Goldilocks algorithm :

```
java -Xmx10000m -Xms10000m -classpath /path/to/rapid/rapid.jar:/path/to/rapid/lib/* Goldilocks -p=/path/to/base_folder/sub_folder -f=rv -s
```

4. Run Goldilocks algorithm :

```
java -Xmx10000m -Xms10000m -classpath /path/to/rapid/rapid.jar:/path/to/rapid/lib/* LockSet -p=/path/to/base_folder/sub_folder -f=rv -s
```
