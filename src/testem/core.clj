(ns ^{ :doc "Framework Detection for lein-testem."
       :author "Yannick Scherer" }
  testem.core
  (:require [clojure.math.combinatorics :refer [subsets permutations]]))

;; ## Concept
;;
;; Aside from detecting test frameworks (and the profiles necessary to execute them), 
;; lein-testem wants to facilitate testing against different versions of libraries.
;;
;; If a profile overwrites an artifact in the top-level dependencies, it is safe to assume
;; that one wants to test against different configurations/combinations of artifacts. This holds, 
;; most prominently, for different Clojure versions but can be extended to anything one wants
;; to be compatible to.

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

(defn overwriting-profiles
  "Create map of `{ profile { overwritten-artifact new-version ... } ... }` based on a map
   created by `project-artifacts`. This will also include artifacts that are not in the 
   top-level dependencies."
  [{:keys [dependencies] :as artifact-map}]
  (let [profile-map (reduce
                      (fn [m [artifact profiles]]
                        (let [initial-version (profiles nil)]
                          (reduce
                            (fn [m [profile version]]
                              (if (= version initial-version)
                                m
                                (assoc-in m [profile artifact] version)))
                            m profiles)))
                      {} dependencies)]
    ;; filter duplicate overwrite maps
    (->> profile-map
      (group-by second)
      (map (juxt (comp first first second) first))
      (into {}))))

;; ## Test

(def T  {:dependencies '[[org.clojure/clojure "1.5.1"]  [b "1.0.0"] [c "0.1.0"]]
         :profiles  {:x  {:dependencies '[[org.clojure/clojure "1.4.0"]  [b "1.0.0"]]} 
                     :y  {:dependencies '[[org.clojure/clojure "1.4.0"]  [b "1.0.0"]  [c "0.1.1"]]}
                     :yy {:dependencies '[[org.clojure/clojure "1.4.0"]  [b "1.0.0"]  [c "0.1.1"]]}
                     :z  {:dependencies '[[d "0.1"]]}
                     }})

(def P (project-artifacts T))
