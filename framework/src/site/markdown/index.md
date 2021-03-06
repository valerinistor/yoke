Yoke is a minimal and flexible polyglot middleware framework for [Vert.x](http://www.vertx.io), providing a robust set
of features for building single and multi-page, and hybrid web applications.


### Middleware

Build quickly robust applications using the user-friendly API and existing middleware at your disposal. To get details
on the currently implemented middleware look at the list in the navigation bar.


### Performance

Yoke provides a thin layer of features fundamental to any web application, without obscuring features that you know and
love in Vert.x.


#### Java

``` java
Yoke yoke = new Yoke(vertx)
  .use(new Favicon())
  .use(new Static("webroot"))
  .use(new Router()
    .all("/hello", new Handler<HttpServerRequest>() {
      @Override
      public void handle(HttpServerRequest request) {
        request.response().end("Hello World!");
      }
    })).listen(8080);
```

[Java API reference](reference/java.html)


#### Groovy

``` groovy
def yoke = new GYoke(vertx)
  .use(new Favicon())
  .use(new Static("webroot"))
  .use(new GRouter()
    .all("/hello") { request ->
      request.response().end("Hello World!");
    }).listen(8080)
```

[Groovy API reference](reference/groovy.html)


#### JavaScript

``` js
var yoke = new Yoke()
  .use(new Favicon())
  .use(new Static('webroot'))
  .use(new Router()
    .all("/hello", function (request) {
      request.response().end('Hello World!');
    })).listen(8080);
```

[JavaScript API reference](reference/javascript.html)


### Example Apps

* [KitCMS](https://github.com/pmlopes/yoke/tree/master/example/kitcms) - Remake of KitCMS from NodeJS with Yoke
* [Persona](Persona.html) - Implementation of Mozilla Persona Authentication with Yoke


### Benchmarks

* [Benchmark](Benchmark.html) Simple Benchmarks Yoke vs [ExpressJS](http://expressjs.com)


### Extras

* [Rest](Rest.html) Json REST Router


### 3rd-party

* [Google Closure templates](https://github.com/core9/vertx-yoke-engine-closure) Yoke templating engine for Google Closure templates


### Links

* GitHub [repository](https://github.com/pmlopes/yoke)
* [Vert.x](http://vertx.io)
* [Google Group](https://groups.google.com/forum/?fromgroups#!forum/yoke-framework)
* [Me](http://www.jetdrone.com)
