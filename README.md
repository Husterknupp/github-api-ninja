github-api-ninja
================

GitHub API client with some statistics on public repositories. Show language statistics on regarding public 
repositories on GitHub, in the form `Java: 37,3817 %`. Currently I only respect the first five because of DOS 
prevention on GitHub site. Statistics are printed out on `System.out`.

**Results**

Statistics of the first 100 repositories (Jan 16, 2015)

```
bytesTotal: 35090025
Io: 0.048000 %
D: 0.014100 %
Scheme: 0.227600 %
Arc: 0.579500 %
ActionScript: 0.012100 %
Ruby: 75.048200 %
C: 1.749000 %
CSS: 0.388200 %
CoffeeScript: 0.001800 %
Perl: 0.732100 %
Ragel in Ruby Host: 0.016600 %
Emacs Lisp: 0.046000 %
PHP: 0.003000 %
Objective-C: 0.000100 %
Erlang: 4.595500 %
Shell: 0.112200 %
Python: 0.486300 %
C++: 9.397900 %
JavaScript: 6.521000 %
Makefile: 0.021000 %
```

**Run**

1. `mvn clean package`
2. `java -jar target/github-api-ninja-1.0-SNAPSHOT-jar-with-dependencies.jar`

**Extensions**

- Mock HTTP connection: http://wiremock.org/
- Persist the data a) to build some cache and avoid GitHub overload and b) to play around a little with a persistence
 unit.
- Make a standalone webserver application. Use Spring Boot, for instance. Is [JHipster](https://jhipster.github.io) an option for this? Probably not because I dont know Angular...
- Build a RESTful HTTP interface on top. E.g., `GET /statistics`, `GET /statistics/language`, ... seem reasonable.
- On top of that a simple and focused GUI/Frontend would be cool. Enter a language name and receive the result in a
beautifully nice way.

**Done**
- Provide way to log in via the GitHub API, i.e., receive more than 5 repo statistics.
  - See developer.github.com/v3/oauth_authorizations
  - For OAuth, use this library: https://github.com/Kobee1203/scribe-java

**More new stuff**

- nice html-templates: http://ink.sapo.pt/, http://purecss.io/
- learn jquery: https://www.codeschool.com/courses/try-jquery
- tryout purecss with jquery for example on any public api: [http://plnkr.co/](http://plnkr.co/edit/owTWPHW3xZDE8vnHn7ra?p=catalogue)
- platform as a service: https://www.heroku.com/home
- when it comes to a search index: [elastic search](http://www.elasticsearch.org/overview/elasticsearch/)
