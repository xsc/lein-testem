(ns testem.core-test
  (:require [midje.sweet :refer :all]
            [testem.core :refer :all]))

(def test-project-data
  '{:dependencies [[org.clojure/clojure "1.5.1"]
                   [something "1.0.0"]]
    :plugins [[lein-something "1.2.3"]]
    :profiles {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
               :1.6 {:dependencies [[org.clojure/clojure "1.6.0-master-SNAPSHOT"]]}
               :old {:dependencies [[org.clojure/clojure "1.4.0"]]}
               :1.4+ {:dependencies [[org.clojure/clojure "1.4.0"] [some-logging "0.1.0"]]}
               :log {:dependencies [[some-logging "0.1.0"]]}
               :legacy {:plugins [[lein-something "0.8.0"]]}}})

(fact "about `project-artifacts`"
  (project-artifacts test-project-data)
      => '{:dependencies {org.clojure/clojure {nil "1.5.1" 
                                               :old "1.4.0"
                                               :1.4 "1.4.0" 
                                               :1.4+ "1.4.0"
                                               :1.6 "1.6.0-master-SNAPSHOT"}
                          something {nil "1.0.0"}
                          some-logging {:1.4+ "0.1.0"
                                        :log "0.1.0"}}
           :plugins {lein-something {nil "1.2.3" 
                                     :legacy "0.8.0"}}})

(fact "about `overwriting-profiles`"
  (let [artifact-map (project-artifacts test-project-data)
        profile-map (overwriting-profiles artifact-map)]
    (or (:old profile-map) (:1.4 profile-map)) => '{org.clojure/clojure "1.4.0"}
    (:1.6 profile-map) => '{org.clojure/clojure "1.6.0-master-SNAPSHOT"}
    (:1.4+ profile-map) => '{org.clojure/clojure "1.4.0" some-logging "0.1.0"}
    (:log profile-map) => '{some-logging "0.1.0"}))
