# World Bank Challenge

The Scenario We need a program which call pull/download WorldBank world population and GDP data into a local database and then query that database to answer two question which are important to our global marketing team:

1. Which 10 countries in the data have the highest population growth rates from 2010 to 2018?

2. Of those 10 countries, when combined with GDP/PPP, which would be the best 3 countries to start investing marketing into for our products? The challenge should be completed in (one of the allowed) functional programming language, with submission details found in the “Code Submission” and “Completed Challenge” sections below.

Data Details Details about the Data API can be found here: https://datahelpdesk.worldbank.org/knowledgebase/articles/898581-api-basic-call-structures Details about the available datasets in the WorldBank Databank can be found here: https://datacatalog.worldbank.org/ Part of this challenge is figuring out how to use the WorldBank API successfully to acquire the data that you need. You will need both Population data (per year) AND Gross Domestic Product (GDP) in Purchasing-Power-Parity (PPP) data (per year) for all of the countries in the WorldBank data-set to answer the above questions! You can store those data however you would like in your data schema, but keep in mind that you will need to do (at least) two separate requests to the WorldBank API to get both the Population data and GDP (PPP) data.

Code Submission You are free to use any of the following functional programming languages for your submission:

· Haskell · Scala - Preferred

If using a language on the JVM (like Scala or Clojure), please do not use any of the following frameworks:

· Apache Spark, Spring, Grails, Hibernate, JBeans, JSF, Apache Struts

· Any other Java library that does not have an explicit Scala/Clojure SDK!

· If the library was only designed to be used with Java, then don’t use it!

Example(s) of allowed JVM frameworks: · Anything from Typelevel (strongly encouraged)

· Akka (any), Play Framework, Lightbend Config, Compojure

· Any pure functional library/framework (e.g. ZIO, Arrow, Cats)

Please do not use Enterprise Java/C# Patterns or GoF Patterns in your submission (no “DataCollectorAbstractFactories” or “IabstractResultAdapters”)!

If your submission code looks like or is organized like this: · https://github.com/EnterpriseQualityCoding/FizzBuzzEnterpriseEdition

your submission will be rejected!

Please do use Functional design patterns, e.g. “(pure) functions”, in your submission. Example: · https://www.slideshare.net/ScottWlaschin/fp-patterns-ndc-london2014

Challenge Details:

You will need to write a program that performs the following tasks:

Data Ingestion

Start by find the best sources for world population by country and GDP/PPP by country from 2010 to 2018 in the WorldBank Databank (see “World Development Indicators” or the “SP.POP.TOTL” dataset specifically, although there are probably other datasets with the necessary data).

Your program should:

1. Collect the world population data from the WorldBank Databank through their Data API

2. Collect the world Gross Domestic Product (GDP) / Purchasing-Power-Parity (PPP) data from the WorldBank Databank through their Data API

3. Parse those data and store them into an embedded database (if you are not sure what to use, try SQL Server Embedded, H2 database, or SQLite)

You can use whatever table schema you would like, but do keep in mind that you’ll be querying those data later!

Data Query

Your program should:

1. Query the world population table for the top 10 countries by population growth from 2010 to 2018. Year-to-Year grown is defined here as the difference between Y_n+1 – Y_n, and highest growth over a time period will be defined as the maximum sum of the Year-to-Year grown differences in that time period.

2. Query the the GDP/PPP table for the top 3 countries by GDP growth, limited to the subset of countries from #1. This function should be independent from the function in #1 (i.e. this function shouldn’t call the function from #1 to get the highest population countries – use SQL instead!).

Performing part of the operation in SQL and part of it in code is acceptable. Just don’t do any SQL joins in your code!

Completed Challenge

A completed challenge ready to submit for review will include:

1. An Zip archive with your source code, DDL, notes, etc. as well as any dependencies needed for your build tool to compile your program. No pre-compiled programs please!

2. In the root of the archive, a single script file (shell script, or Windows .cmd file) which will compile your program, named “compile.sh” or “compile.cmd”. Any dependencies your program needs to function (i.e. the embedded database driver and the HTTP library) should be downloaded by your build tool.

3. In the root of the archive, a single script file (shell script, or Windows .cmd file) which will run your program, named “run.sh” or “run.cmd”.

4. Your program should accept exactly two (separate) command line arguments:

1. --dataload – which should perform the data ingestion from Worldbank

2. --results – which should perform the data queries and print to the console the two results

Testing/Review Environment

The reviewers will have the following pre-installed and should not be in your archive: Java 8 (latest) and .NET core (latest).

If you are not using a JVM or .NET core language, and your chosen language requires a runtime/ virtual-machine, please include instructions for setting that up (e.g. ErlangVM).

Grading

Your submission will be reviewed on the following basis:

1. Does the program complete the described objectives correctly?

2. Does the program compile with “compile.sh/cmd” and run with “run.sh/cmd”?

3. Code quality, clarity, conciseness, and failure cases handled

4. SQL queries used to gather the results

5. Tests, configuration, and additional documentation are appreciated, but are not required

# Dependencies

* JVM 1.8 or above
* sbt
* curl

## Run application

```shell
./compile.sh
./run.sh --dataload # to run load data
./run.sh --results # to query results
```
