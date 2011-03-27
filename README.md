# tailog

A partial clone of tail, the GNU command line utility.
Currently it does not implement the -f (follow) feature.

# Command line usage

> $ java -jar tailog-jar-with-dependencies.jar -?
>                                                   
> Usage: tailog [options]... [FILE]...
> Print the last 10 lines of each FILE to standard output.
> With more than one FILE, precede each with a header giving the file name.
> Option                                  Description
> ------                                  -----------
> -?, --help                              display this help and exit
> -d, --debug                             enable debugging statements
> -n, --lines <Integer: K>                output the last K lines, instead of
>                                         the last 10
> -q, --quiet, --silent                   never output headers giving file names
> --version                               output version information and exit

kejhrkehrkehkrhkh


# How to compile

* To produce the artifact __tailog-jar-with_dependencies.jar__, run
> $ mvn package
* To produce the javadoc, run
> $ mvn javadoc:javadoc

# Documentation

* [javadoc](http://example.com/ "Javadoc")

# Licence

Copyright (C) 2011 Gavin Bong. Distributed under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html "license details").

