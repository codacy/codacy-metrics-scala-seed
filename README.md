# Codacy Metrics Scala Seed

[![Codacy Code Quality Badge](https://api.codacy.com/project/badge/Grade/84f9864f373e4228b8c030d583bd4094)](https://www.codacy.com/app/Codacy/codacy-metrics-scala-seed?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=codacy/codacy-metrics-scala-seed&amp;utm_campaign=Badge_Grade)
[![Codacy Code Coverage Badge](https://api.codacy.com/project/badge/Coverage/84f9864f373e4228b8c030d583bd4094)](https://www.codacy.com/app/Codacy/codacy-metrics-scala-seed?utm_source=github.com&utm_medium=referral&utm_content=codacy/codacy-metrics-scala-seed&utm_campaign=Badge_Coverage)
[![CircleCI](https://circleci.com/gh/codacy/codacy-metrics-scala-seed.svg?style=svg)](https://circleci.com/gh/codacy/codacy-metrics-scala-seed)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-metrics-scala-seed/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-metrics-scala-seed)

Framework to help integration with external metrics tools at Codacy.

## :warning: This project is under development and not production ready

For more details and examples of tools that use this project, you can check:

* [codacy-metrics-cloc](https://github.com/codacy/codacy-metrics-cloc)

## Usage

Add to your SBT dependencies:

`"com.codacy" %% "codacy-metrics-scala-seed" % "<VERSION>"`

## Docs

### How to integrate an external metrics tool on Codacy

By creating a docker and writing code to handle the tool invocation and output,
you can integrate the tool of your choice on Codacy!

> To know more about dockers, and how to write a docker file please refer to [https://docs.docker.com/reference/builder/](https://docs.docker.com/reference/builder/)

In this tutorial, we explain how you can integrate a metrics tool of your choice in Codacy.
You can check the code of an already implemented tool and if you wish fork it to start your own.
You are free to modify and use it for your own tools.

#### Requirements

* Docker definition with the tool you want to integrate

#### Assumptions and Behaviour

* To run the tool we provide the configuration file, `/.codacyrc`, with the language to run and optional parameters your tool might need.
* The source code to be analysed will be located in `/src`, meaning that when provided in the configuration, the file paths are relative to `/src`.

* **.codacyrc**
  * **files:** Files to be analysed (their path is relative to `/src`)
  * **language:** Language to run the tool

```json
{
  "files" : [ "foo/bar/baz.scala", "foo2/bar/baz.scala" ],
  "language": "Scala"
}
```

** Notes: **

* To run the tool in debug mode so you can have more detailed logs, you have just need to set the environment variable `METRICS_DEBUG` to `true` when invoking the docker.
* To configure a different timeout for the tool you have to set the environment variable `METRICS_TIMEOUT` when invoking the docker, setting it with values like `10 seconds`, `30 minutes` or `2 hours`.

#### Output

You are free to write the code running inside the docker in the language you prefer.
After you have your results from the tool, you should print them to the standard output in our **Result** format, one result per line.

```json
{
  "filename": "path/to/my/file1.scala",
  "complexity": 1,
  "loc": 300,
  "cloc": 320,
  "nrMethods": 20,
  "nrClasses": 2,
  "lineComplexities": [
    {
      "line": 2, 
      "value": 3
    }
  ]
}
```

> The filename should not include the prefix `/src/`, the absolute path `/src/folder/file.js` should be returned as `folder/file.js`.

#### Submit the Docker

** Running the docker **

```sh
docker run -t \
    --net=none \
    --privileged=false \
    --cap-drop=ALL \
    --user=docker \
    --rm=true \
    -v <PATH-TO-FOLDER-WITH-FILES-TO-CHECK>:/src \
    -v <PATH-TO-CODACY-CONFIG-RC>:/.codacyrc \
    <YOUR-DOCKER-NAME>:<YOUR-DOCKER-VERSION>
```

** Docker restrictions **

    * Docker image size should not exceed 500MB
    * Docker should contain a non-root user named docker with UID/GID 2004
    * All the source code of the docker must be public
    * The docker base must officially supported on DockerHub
    * Your docker must be provided in a repository through a public git host (ex: GitHub, Bitbucket, ...)

** Docker submission **

    * To submit the docker you should send an email to `team [at] codacy [dot] com` with the link to the git repository with your docker definition.
    * The docker will then be subjected to a review by our team and you will then be contacted with more details

If you have any question or suggestion regarding this guide let us know.

## Test

> TODO

## What is Codacy

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features

* Identify new Static Analysis issues
* Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
* Auto-comments on Commits and Pull Requests
* Integrations with Slack, HipChat, Jira, YouTrack
* Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
