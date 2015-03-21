# Zero-log

<tt>zero-log</tt> is a logging framework for Scala, which is designed to take advantage of Scala feature to let logging faster and simpler. Imported from http://code.google.com/p/zero-log by ericpony.

## <a name="Why_use_zero-log_instead_of_log4j_,_logback_,_etc..."></a>Why use <tt>zero-log</tt> instead of <tt>log4j</tt>, <tt>logback</tt>, etc...[](#Why_use_zero-log_instead_of_log4j_,_logback_,_etc...)

*   Zero cost if the log is disabled by compile-time configuration.
*   Zero XML or Propertis files you need to write.
*   10+ times faster for string formatting than any other Java logging libraries (log4j, logback, ...).
*   Support <tt>@scala.annotation.elidable</tt> and <tt>scala.util.logging.Logged</tt>.

## <a name="Performance"></a>Performance[](#Performance)

<tt>zero-log</tt> is extremely fast. 

Thanks to Scala's <tt>@elidable</tt>, when using compile-time configuration, the cost for disabled logs can be exactly zero. 

<tt>zero-log</tt>'s string formatting can be 10+ times faster than any other Java logging libraries. See [Fastring](https://github.com/Atry/fastring) for more information. 

## <a name="Usage"></a>Usage[](#Usage)

You only need call <tt>ZeroLoggerFactory.newLogger(this)</tt> to get the logger, and call <tt>logger.info()</tt> to log. No more configurations for simple use. 

    package com.yourDomain.yourProject
    object Sample {
      implicit val (logger, formatter, appender) = ZeroLoggerFactory.newLogger(this)
    
      def main(args: Array[String]) {
        logger.info("Logging in a Singleton.")
        logger.fine("Hello,")
        logger.warning("World!")
        logger.finest(fast"Faster string formatting: args.length is ${args.length}")
        new Sample
      }
    }
    
    class Sample {
      import Sample._
    
      logger.info("Logging in a class instance.")
      logger.finer("Hello,")
      logger.severe("World!", new Exception("With some Exception"))
    }

Note: By default, only logging level <tt>info</tt>, <tt>warning</tt> and <tt>severe</tt> are enabled, and the other levels are disabled. 

## <a name="Configure_logging_level,_formatting,_or_target"></a>Configure logging level, formatting, or target[](#Configure_logging_level,_formatting,_or_target)

Unlike <tt>log4j</tt>, <tt>logback</tt>, <tt>java.util.logging</tt>, the <tt>zero-log</tt> does not load any XML or Properties file as configuration. Instead, <tt>zero-log</tt> use <tt>ZeroLoggerFactory</tt> to configure logging level, logging formatting, or logging target. If you need custom configuration, just put your own <tt>ZeroLoggerFactory</tt> on scalac's source path. 

For example, you may create a file at <tt>src/main/scala/zero-log.config.scala</tt> to change logging level for <tt>com.yourDomain.yourProject.Sample</tt>: 

    import com.dongxiguo.zeroLog.Filter
    import com.dongxiguo.zeroLog.formatters.SimpleFormatter
    import com.dongxiguo.zeroLog.appenders.ConsoleAppender
    
    // Set global default logging level to Warning, and send logs to ConsoleAppender
    object ZeroLoggerFactory {
      final def newLogger(singleton: Singleton) =
        (Filter.Warning, SimpleFormatter, ConsoleAppender)
    }
    
    package com.yourDomain.yourProject {
      object ZeroLoggerFactory {
        // Set package com.yourDomain.yourProject's default logging level to Info
        final def newLogger(singleton: Singleton) =
          (Filter.Info, SimpleFormatter, ConsoleAppender)
    
        // Set Sample's logging level to Finest
        final def newLogger(singleton: Sample.type) =
          (Filter.Finest, SimpleFormatter, ConsoleAppender)
      }
    }

Look at logger's initializing code in <tt>Sample.scala</tt>: 

    implicit val (logger, formatter, appender) = ZeroLoggerFactory.newLogger(this)

When <tt>Sample.scala</tt> is compiled with <tt>zero-log.config.scala</tt>, <tt>ZeroLoggerFactory.newLogger</tt> will be resolved as <tt>com.yourDomain.yourProject.ZeroLoggerFactory.newLogger</tt>. So <tt>logger</tt> and <tt>formatter</tt> will be the result of <tt>newLogger</tt> you defined at <tt>zero-log.config.scala</tt>, which are <tt>Filter.Finest</tt> , <tt>SimpleFormatter</tt>, and <tt>ConsoleAppender</tt> for <tt>Sample</tt>. 

## Run-time configuration

When you create a library, you may want the user of your library to be able to change logging settings without change your library's source code. 

Don't worry. <tt>zero-log</tt> can resolve <tt>ZeroLoggerFactory</tt> by reflection. Just call <tt>ZeroLoggerFactory.newLogger</tt> without <tt>com.yourDomain.yourProject.ZeroLoggerFactory</tt> in source path, and ship your library without <tt>com.yourDomain.yourProject.ZeroLoggerFactory</tt> in your release. Your users can define their own <tt>com.yourDomain.yourProject.ZeroLoggerFactory</tt> or <tt>com.yourDomain.ZeroLoggerFactory</tt>, which are resolved by <tt>zero-log</tt> at run-time.

Note that you still need to recompile the configuration file and put it in the class path when you change your configuration, but you don't need to recompile your library. 
