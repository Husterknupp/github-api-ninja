github-api-ninja
================

GitHub API client with some statistics on public repositories. Show language statistics on regarding public 
repositories on GitHub, in the form `Java: 37,3817 %`. Currently I only respect the first five because of DOS 
prevention on GitHub site. Statistics are printed out on `System.out`.

**Results**

Statistics of the first 50 repositories (Dec 28, 2014)

```
bytesTotal: 16732869
D: 0.02947492148537109800 %
ActionScript: 0.02533337229855800600 %
Ruby: 68.92710986980176561500 %
C: 3.65037818678912743500 %
CSS: 0.05196956959383355000 %
CoffeeScript: 0.00007769139888682600 %
Perl: 1.53456648707403374800 %
Ragel in Ruby Host: 0.03487148557727906700 %
Objective-C: 0.00018526410503781500 %
Shell: 0.21458364372541254000 %
Python: 0.12648159738775221400 %
C++: 19.48550484677791955500 %
JavaScript: 5.87532837315585271100 %
Makefile: 0.04413469082916982100 %
```

**Run**

1. `mvn clean package`
2. `java -jar target/github-api-ninja-1.0-SNAPSHOT-jar-with-dependencies.jar`
3. `java -jar target/github-api-ninja-1.0-SNAPSHOT-jar-with-dependencies.jar 50` for statistics on the first 50 repos.

**Extensions**

- Provide way to log in via the GitHub API, i.e., receive more than 5 repo statistics. 
  - See developer.github.com/v3/oauth_authorizations
  - For OAuth, use this library: https://github.com/Kobee1203/scribe-java
- Build a RESTful HTTP interface on top. E.g., `GET /statistics`, `GET /statistics/language`, ... seem reasonable.
- On top of that a simple and focused GUI/Frontend would be cool. Enter a language name and receive the result in a 
beautifully nice way. 
- Persist the data a) to build some cache and avoid GitHub overload and b) to play around a little with a persistence
 unit.
- Make a standalone webserver application. Use Spring Boot, for instance. Is [JHipster](https://jhipster.github.io) an option for this? Probably not because I dont know Angular...
- Mock HTTP connection: http://wiremock.org/

**More new stuff**

- nice html-templates: http://ink.sapo.pt/, http://purecss.io/
- learn jquery: https://www.codeschool.com/courses/try-jquery
- tryout purecss with jquery for example on any public api: [http://plnkr.co/](http://plnkr.co/edit/owTWPHW3xZDE8vnHn7ra?p=catalogue)
- platform as a service: https://www.heroku.com/home
