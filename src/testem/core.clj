(ns ^{ :doc "Framework Detection for lein-testem."
       :author "Yannick Scherer" }
  testem.core
  (:require [clojure.math.combinatorics :refer [subsets permutations]]))

;; ## Concept
;;
;; Aside from detecting test frameworks (and the profiles necessary to execute them), 
;; lein-testem wants to facilitate testing against different versions of libraries.
;;
;; If I have a certain dependency in more than one profile (and those profiles are not
;; identical) it is safe to assume I want to test against different configurations/combinations
;; of artifacts. This holds, most prominently, for different Clojure versions but can be extended
;; to things like logging implementations, network libraries, etc...

;; ## Utilities

(defn- create-profile-keys
  "Create pairs of `[profile key]` (where key is destined for `get-in`) to access the given 
   field on a project and profile basis (e.g. `:dependencies`, `:plugins`, ...)."
  [project k]
  (->> (keys (:profiles project))
    (map #(vector % [:profiles % k]))
    (cons [nil [k]])))

(defn- create-profile-artifact-map
  "Create nested map of `{ artifact { profile version ...} ... } ... }` using a given key."
  [project k]
  (let [ks (create-profile-keys project k)]
    (reduce
      (fn [m [profile k]]
        (let [artifacts (get-in project k)]
          (reduce
            (fn [m [artifact version & _]]
              (update-in m [artifact] assoc profile version))
            m artifacts)))
      {} ks)))

(defn project-artifacts
  "Create map of dependencies/plugins contained in a project, associating them
   with the different versions contained in different profiles (see `create-profile-artifact-map`)."
  [project]
  (->> [:dependencies :plugins]
    (map (juxt identity (partial create-profile-artifact-map project)))
    (into {})))

(defn find-overwritten-dependencies
  "Find dependencies that are available in the user profile and at least in one
   other profile, using a different version. Input is a map created by `project-artifacts`."
  [{:keys [dependencies] :as artifact-map}]
  (->> dependencies
    (map
      (fn [[artifact profiles]]
        [artifact (->> profiles
                    (filter (comp not #{(profiles nil)} second))
                    (into {}))]))
    (filter (comp not empty? second))
    (into {})))

;; ## Test

(def T  {:dependencies '[[a 0]  [b 0] [c 2]]
         :profiles  {:x  {:dependencies '[[a 1]  [b 0]]} 
                     :y  {:dependencies '[[a 1]  [b 0]  [c 3]]}}})

(def P (project-artifacts T))
