(defproject lein-testem "0.1.0-SNAPSHOT"
  :description "Minimal-Configuration Testing for Clojure."
  :url "https://github.com/xsc/lein-testem"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[midje "1.5.1"]]
                   :plugins [[lein-midje "3.1.1"]]}}
  :eval-in-leiningen true)
