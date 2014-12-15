github-api-ninja
================

GitHub API client with some statistics on public repositories. Show language statistics on regarding public 
repositories on GitHub, in the form `Java: 37,3817 %`. Currently I only respect the first five because of DOS 
prevention on GitHub site. Statistics are printed out on `System.out`.

Run via `mvn -Dtest=IntegrationTest test`

**Extensions**

- Provide way to log in via the GitHub API, i.e., receive more than 5 repo statistics.
- Build a RESTful HTTP interface on top. E.g., `GET /statistics`, `GET /statistics/language`, ... seem reasonable.
- On top of that a simple and focused GUI/Frontend would be cool. Enter a language name and receive the result in a 
beautifully nice way. 
- Persist the data a) to build some cache and avoid GitHub overload and b) to play around a little with a persistence
 unit.
- Make a standalone webserver application. Use Spring Boot, for instance. 

**Frontend Stuff**

- nice html-templates: http://ink.sapo.pt/, http://purecss.io/
- learn jquery: https://www.codeschool.com/courses/try-jquery
- tryout purecss with jquery for example on any public api: [http://plnkr.co/](http://plnkr.co/edit/owTWPHW3xZDE8vnHn7ra?p=catalogue)
