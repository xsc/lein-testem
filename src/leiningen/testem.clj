(ns ^{ :doc "Run Tests on your Projects with minimal Configuration."
       :author "Yannick Scherer"}
  leiningen.testem
  (:require [testem.core :refer [create-single-test-tasks]]
            [leiningen.core.main :as main]))

(defn ^:higher-order testem
  "Run Tests on your Projects with minimal Configuration.

   Usage:

     lein testem [:autotest]

   Supported Test Frameworks:

     clojure.test
     midje        (https://github.com/marick/Midje)
     speclj       (http://speclj.com)
  "
  [project & args]
  (let [tasks (create-single-test-tasks project)
        autotest? (contains? (set args) ":autotest")]
    (try
      (binding [main/*exit-process?* false]
        (doseq [[framework {:keys [test autotest]}] tasks]
          (main/info (str "Testing with '" framework "' ..."))
          (doseq [[task-name & task-args :as task] (if autotest? autotest test)]
            (main/info "--" (pr-str task))
            (binding [main/*debug* nil
                      main/*info* nil]
              (main/apply-task task-name project task-args)))))
      (catch Exception ex (main/abort "Tests failed.")))))
