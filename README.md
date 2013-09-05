# lein-testem

A [Leiningen](https://github.com/technomancy/leiningen) plugin that let's you test your Clojure projects
with reduced configuration effort.

[![Build Status](https://travis-ci.org/xsc/lein-testem.png)](https://travis-ci.org/xsc/lein-testem)
[![endorse](https://api.coderwall.com/xsc/endorsecount.png)](https://coderwall.com/xsc)

__Experimental!__ This project is in flux and by no means stable (or maybe it is, no one knows yet).
Feel free to open issues to help with lein-testem's development!

## Usage

__Leiningen__ ([via Clojars](https://clojars.org/lein-testem))

Put the following into the `:plugins` vector of the `:user` profile in your `~/.lein/profiles.clj`:

```clojure
[lein-testem "0.1.0-alpha1"]
```

This plugin is destined for Leiningen >= 2.0.0.

__Command Line__

You can use lein-testem to automatically detect your current test framework(s) and run available tests.

```bash
$ lein testem 
```

The plugin will create profile combinations for testing by looking for those that overwrite/extend the 
top-level dependencies. Let's assume your `project.clj` looks like this:

```clojure
(defproject my-project "0.1.0-SNAPSHOT"
  ...
  :dependencies [[org.clojure/clojure "1.5.1"] ...]
  :profiles {:test {:dependencies [[midje "1.5.1"]]
                    :plugins [[lein-midje "3.1.1"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}
  ...)
```

lein-testem will detect that you're using Midje (in the profile `:test`) for testing and that the 
profile `:1.4` overwrites the artifact `org.clojure/clojure` of the top-level dependencies. It will
then create a command similar to the following:

```bash
$ lein with-profile dev,test:dev,test,1.4 midje
```

You can run autotest functionality where available:

```bash
$ lein testem :autotest
```

## Supported Frameworks

- [clojure.test](http://richhickey.github.io/clojure/clojure.test-api.html)
- [midje](https://github.com/marick/midje)
- [Speclj](http://speclj.com)
- ...

## Roadmap

- automatically provide/run common test scenarios (e.g. "test against Clojure versions >= 1.3.0")
- ...

## License

Copyright &copy; 2013 Yannick Scherer

Distributed under the Eclipse Public License, the same as Clojure.
