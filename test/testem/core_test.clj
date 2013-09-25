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

;; ## Basic Tests

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
        profiles (overwriting-profiles artifact-map)]
    profiles => (contains [:1.6 :1.4+ :log])
    (set profiles) => #(or (contains? % :1.4) (contains? % :old))))

(fact "about `combine-profiles`"
  (combine-profiles [:dev] [:x :y :z]) => [[:dev] [:dev :x] [:dev :y] [:dev :z]])


;; ## Detection

(tabular
  (fact "about `detect-frameworks`"
    (let [artifacts (project-artifacts ?project)
          frameworks (detect-frameworks artifacts)]
      (set (keys frameworks)) => (set ?fw)))
  ?project                               ?fw
  '{:dependencies [[midje "1.5.1"]]
    :plugins [[lein-midje "3.1.2"]]}     [:midje]
  '{:dependencies [[speclj "1.5.1"]]
    :plugins [[speclj "3.1.1"]]}         [:speclj :clojure.test]
  '{}                                    [:clojure.test])


;; ## Task Creation

(tabular
  (fact "about `create-test-tasks`"
    (let [tasks (create-test-tasks ?project)]
      (set (filter (complement nil?) (map :test (vals tasks)))) => (set ?test)
      (set (filter (complement nil?) (map :autotest (vals tasks)))) => (set ?autotest)))
  ?project ?test ?autotest
  '{:dependencies [[midje "1.5.1"]]
    :plugins [[lein-midje "3.1.2"]]}
  [["with-profile" "dev" "midje"]] 
  [["with-profile" "dev" "midje" ":autotest"]]

  '{:dependencies [[midje "1.5.1"] [org.clojure/clojure "1.5.1"]]
    :plugins [[lein-midje "3.1.2"]]
    :profiles {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}}
  [["with-profile" "dev:dev,1.4" "midje"]] 
  [["with-profile" "dev" "midje" ":autotest"]]

  '{:dependencies [[midje "1.5.1"] [org.clojure/clojure "1.5.1"]]
    :plugins [[lein-midje "3.1.2"]]
    :profiles {:log {:dependencies [[logback "1.4.0"]]}}}
  [["with-profile" "dev:dev,log" "midje"]] 
  [["with-profile" "dev" "midje" ":autotest"]]

  '{:dependencies [[org.clojure/clojure "1.5.1"]]
    :profiles {:test {:dependencies [[midje "1.5.1"]]
                      :plugins [[lein-midje "3.1.2"]]}
               :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}}  
  [["with-profile" "dev,test:dev,test,1.4" "midje"]] 
  [["with-profile" "dev,test" "midje" ":autotest"]]

  '{:dependencies [[speclj "1.5.1"]]
    :plugins [[speclj "3.1.1"]]}
  [["with-profile" "dev" "spec"] ["with-profile" "dev" "test"]] 
  [["with-profile" "dev" "spec" "-a"]]

  '{:dependencies [[speclj "1.5.1"] [org.clojure/clojure "1.5.1"]]
    :plugins [[speclj "3.1.1"]]
    :profiles {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}}
  [["with-profile" "dev:dev,1.4" "spec"] ["with-profile" "dev:dev,1.4" "test"]] 
  [["with-profile" "dev" "spec" "-a"]]

  '{:dependencies [[speclj "1.5.1"] [org.clojure/clojure "1.5.1"]]
    :plugins [[speclj "3.1.1"]]
    :profiles {:log {:dependencies [[logback "1.4.0"]]}}}
  [["with-profile" "dev:dev,log" "spec"] ["with-profile" "dev:dev,log" "test"]] 
  [["with-profile" "dev" "spec" "-a"]]
  )

(fact "about `create-single-test-tasks`"
  (let [tasks (create-single-test-tasks 
                '{:dependencies [[org.clojure/clojure "1.5.1"]]
                  :profiles {:test {:dependencies [[midje "1.5.1"]]
                                    :plugins [[lein-midje "3.1.2"]]}
                             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}})]
    (keys tasks) => [:midje]
    (get-in tasks [:midje :autotest]) => ["with-profile" "dev,test" "midje" ":autotest"]
    (get-in tasks [:midje :test]) => (just [["with-profile" "dev,test" "midje"] ["with-profile" "dev,test,1.4" "midje"]])))
