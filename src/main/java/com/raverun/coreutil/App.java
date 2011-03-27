package com.raverun.coreutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.raverun.coreutil.api.ContentObserver;
import com.raverun.coreutil.api.FileTailer;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Command line application
 *
 * @author Gavin Bong
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        OptionParser parser = new OptionParser( );
        OptionSpec<Integer> lines = parser.acceptsAll( Arrays.asList( "n", "lines" ), 
            "output the last K lines, instead of the last 10" )
            .withRequiredArg()
            .ofType( Integer.class )
            .describedAs( "K" );
        parser.acceptsAll( Arrays.asList( "d", "debug" ), "enable debugging statements" );
        parser.acceptsAll( Arrays.asList( "help", "?" ), "display this help and exit" );
        parser.acceptsAll( Arrays.asList( "q", "quiet", "silent" ), "never output headers giving file names" );
        parser.accepts( "version", "output version information and exit" );

        try {
            OptionSet options = parser.parse( args );

            if( options.has( "?" ) || options.has( "help" )) {
                printCommandHeader();
                parser.printHelpOn( System.out );

            } else if( options.has( "version" ) ) {
                printVersion();

            } else {
                boolean enableDebug = false;
                if( options.has( "d" ) || options.has( "debug" ) )
                    enableDebug = true;

                int numberOfLines = 10;
                if( options.has( lines ) ) {
                    Integer k = lines.value( options );
                    numberOfLines = Math.abs( k.intValue() );
                }

                boolean outputFilenameHeader = true;
                if( options.has( "q" ) || options.has( "silent" ) || options.has( "quiet" ) )
                    outputFilenameHeader = false;

//                System.out.println( "Get last " + numberOfLines + "  lines from file." );
                if( numberOfLines >= 0 ) {
                    List<String> fileArgs = options.nonOptionArguments();
                    if( fileArgs.size() == 0 ) {
                    // TODO - die here since it is hard to process CTRL+D or CTRL+Z in cygwin
                        System.out.println( "Filtering from piped inputs or reading from stdin are not implemented. Coming soon!" );
//                        StringWriter writer = readFromStdin();
//                        StringBuffer sb = writer.getBuffer();
//                        System.out.println( sb.toString() );
                    }
                    else {
                        File f = new File( EMPTY );
                        if( enableDebug )
                            System.out.println( "current folder => " + f.getAbsolutePath() + "\n" );

                    // verify first file argument
                        int exitStatus = 0;
                        for( String filename : fileArgs ) {
                            if( enableDebug )
                                System.out.println( "Processing file [" + filename + "]" );

                            int result = handleFile( filename, outputFilenameHeader, 
                                numberOfLines, enableDebug, (fileArgs.size() > 1) );

                            if( enableDebug )
                                System.out.println( "Received exit status: " + result );

                            exitStatus = exitStatus | result;
                        }
                        System.exit( exitStatus );
                    }
                }
            }
        } 
        catch( OptionException e ) {
            System.err.println( App.rationalizeErrorMessage( e.getMessage() ) );
            System.exit( 1 );
        }
    }

    public static void printCommandHeader() {
        System.out.println( "Usage: tailog [options]... [FILE]..." );
        System.out.println( "Print the last 10 lines of each FILE to standard output." );
        System.out.println( "With more than one FILE, precede each with a header giving the file name." );
    }

    /**
     * Convert error message to follow's POSIX tail's message
     */
    public static String rationalizeErrorMessage( String message ) {
        if( message == null )
            return EMPTY;

        if( message.startsWith( "Cannot convert argument" ) &&
            message.indexOf( "of option ['n', 'lines'] to class java.lang.Integer" ) > 0 ) {
            return "tailog: invalid number of lines";
        }

        return message;
    }

    /**
     * This <b>SHOULD</b> a blocking call
     *
     * @param filename
     * @return exit status for the shell
     */
    private static int handleFile( String filename, boolean outputFilenameHeader, 
      int numberOfLines, boolean enableDebug, boolean multipleFiles ) {
        final int[] exitStatus = { 0 };
        final boolean[] indicator = { false };

        final Lock lock = new ReentrantLock();
        final Condition finished = lock.newCondition();

        final File targetFile = new File( filename );
        try {
            lock.lock();
            FileTailer.Builder builder = new FileTailer.Builder( targetFile );

            if( outputFilenameHeader && multipleFiles )
                System.out.println( "==> " + targetFile.getName() + " <==" );

            builder = builder.numberOfLines( numberOfLines )
                          .debug( enableDebug );

            final FileTailer tailer = builder.build();
            tailer.addObserver( new ContentObserver() {
                @Override
                public void onFinishNormal() {
//                    System.out.println( "[App] onFinishNormal()" );
                    System.out.println( EMPTY );
                    try {
                        lock.lock();
                        exitStatus[ 0 ] = 0;
                        indicator[ 0 ] = true;
                        finished.signal();
                    }
                    finally {
                        lock.unlock();
                        tailer.turnOff();
//                        System.out.println( "[App] invoked turnOff" );
                    }
                }

                @Override
                public void onFinishWithException( String error ) {
//                    System.out.println( "[App] onFinishWithException" );
                    System.out.println( error );
                    System.out.println( EMPTY );
                    try {
                        lock.lock();
                        exitStatus[ 0 ] = 1;
                        indicator[ 0 ] = true;
                        finished.signal();
                    }
                    finally {
                        lock.unlock();
                        tailer.turnOff();
//                        System.out.println( "[App] invoked turnOff" );
                    }
                }

                @Override
                public void onNewLine( String line ) {
                    System.out.println( line );
                }
            } );
            tailer.turnOn();
//            tailer.waitInterruptibly();

            while( ! indicator[0] )
                finished.await();
        } 
        catch( FileNotFoundException fnfe ) {
            System.err.println( "tailog: cannot open '" 
                + targetFile.getName() + "' for reading: No such file or directory" );
            exitStatus[ 0 ] = 1;
        } 
        catch( InterruptedException e ) {
            e.printStackTrace();
            exitStatus[ 0 ] = 1;
        }
        finally {
            lock.unlock();
        }

        return exitStatus[ 0 ];
    }

    @SuppressWarnings("unused")
    private static StringWriter readFromStdin() throws IOException {
        StringWriter writer = new StringWriter();

        BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );

        while( in.ready() ) {
            String s = in.readLine();
            System.out.println( "/ " + s + " /" );
            writer.append( s + "\n" );
        }

//        while( (s = in.readLine() ) != null ) {
//            System.out.println( "/ " + s + " /" );
//            writer.append( s + "\n" );
//        }
        return writer;
    }

    public static void printVersion() {
        System.out.println( "tailog 1.0 beta 1" );
        System.out.println( "Copyright (C) 2011 Gavin Bong" );
    }

    public static final String EMPTY = "";
}
