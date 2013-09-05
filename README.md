# lein-testem

A [Leiningen](https://github.com/technomancy/leiningen) plugin that let's you test your Clojure projects
with reduced configuration effort.

[![Build Status](https://travis-ci.org/xsc/lein-ancient.png)](https://travis-ci.org/xsc/lein-ancient)
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

You can use lein-testem to automatically detect your current test framework(s) and run all available tests.

```bash
$ lein testem 
```

## Supported Frameworks

- [clojure.test](http://richhickey.github.io/clojure/clojure.test-api.html)
- [midje](https://github.com/marick/midje)
- ...

## License

Copyright &copy; 2013 Yannick Scherer

Distributed under the Eclipse Public License, the same as Clojure.
