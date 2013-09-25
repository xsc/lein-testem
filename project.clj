(defproject lein-testem "0.1.0-alpha3"
  :description "Minimal-Configuration Testing for Clojure."
  :url "https://github.com/xsc/lein-testem"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[version-clj "0.1.0"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]
                   :plugins [[lein-midje "3.1.2"]]}}
  :eval-in-leiningen true)
