(ns ^{ :doc "A Leiningen plugin to test your projects with reduced configuration effort."
       :author "Yannick Scherer"}
  leiningen.testem
  (:require [testem.core :refer [create-test-tasks]]
            [leiningen.core.main :as main]))

(defn ^:higher-order testem
  "I don't do a lot."
  [project & args]
  (let [tasks (create-test-tasks project)
        autotest? (contains? (set args) ":autotest")]
    (doseq [[framework {:keys [test autotest]}] tasks]
      (let [[task-name & task-args :as task] (if autotest? autotest test)]
        (main/info (str "Testing with '" framework "' ..."))
        (main/info (str "Task: " task))
        (main/apply-task task-name project task-args)))))
