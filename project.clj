(defproject lein-testem "0.1.0-alpha1"
  :description "Minimal-Configuration Testing for Clojure."
  :url "https://github.com/xsc/lein-testem"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/math.combinatorics "0.0.4"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]
                   :plugins [[lein-midje "3.1.1"]]}}
  :eval-in-leiningen true)
