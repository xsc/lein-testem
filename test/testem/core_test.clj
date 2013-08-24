(ns testem.core-test
  (:require [midje.sweet :refer :all]
            [testem.core :refer :all]))

(def test-project-data
  '{:dependencies [[org.clojure/clojure "1.5.1"]
                   [something "1.0.0"]]
    :plugins [[lein-something "1.2.3"]]
    :profiles {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
               :1.6 {:dependencies [[org.clojure/clojure "1.6.0-master-SNAPSHOT"]]}
               :legacy {:plugins [[lein-something "0.8.0"]]}}})

(fact "about `project-artifacts`"
  (project-artifacts test-project-data)
      => '{:dependencies {org.clojure/clojure {nil "1.5.1" 
                                               :1.4 "1.4.0" 
                                               :1.6 "1.6.0-master-SNAPSHOT"}
                          something {nil "1.0.0"}}
           :plugins {lein-something {nil "1.2.3" 
                                     :legacy "0.8.0"}}})
