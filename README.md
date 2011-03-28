# tailog

A partial clone of tail, the GNU command line utility.

# Command line usage

    $ java -jar tailog-jar-with-dependencies.jar -?
     
    Usage: tailog [options]... [FILE]...
    Print the last 10 lines of each FILE to standard output.
    With more than one FILE, precede each with a header giving the file name.
    Option                                  Description
    ------                                  -----------
    -?, --help                              display this help and exit
    -d, --debug                             enable debugging statements
    -n, --lines <Integer: K>                output the last K lines, instead of
                                            the last 10
    -q, --quiet, --silent                   never output headers giving file names
    --version                               output version information and exit


This program will output an exit status code of 0 if everything is fine.
If multiple files are provided; the failure of processing one file will change
the exit status code to 1.

When running on vista in cygwin's bash shell (the test environment), the way to run 
the program is to provide the path to the files in windows convention. E.g.


    $ java -jar tailog-jar-with-dependencies.jar -n 2 
         C:\\_programs\\cygwin177\\root\\home\\gavin\\projects\\datasets\\mac.in

The program will accept files using '__\r__' for line delimiters (pre MAC OS9),
as well as '__\r\n__' (windows) and '__\n__' (unixes).


# How to compile

* To produce the artifact __tailog-jar-with-dependencies.jar__, run the following command. This jar will be produced in the __target__ directory if compilation is successfull.
> $ mvn package
* To produce the javadoc, run
> $ mvn javadoc:javadoc

# Documentation

* [javadoc](http://raverun.com/projects/tailog/ "Javadoc")
* Status of this code:
  + (TODO) implement the -f follow feature.
  + (TODO) If no files are supplied, the program needs to read from stdin.
  + (TEST) The program has only been tested with files up to 10MB in size on windows vista (cygwin) environment.

# Licence

Copyright (C) 2011 Gavin Bong. Distributed under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html "license details").

