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
;;
;; Also, if an artifact is encountered that is not contained within the top-level dependencies,
;; it might be a concrete implementation of an abstract facade (e.g. of slf4j) and should thus be 
;; tested, too.

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
      (map (comp first first second)))))

(defn combine-profiles
  "Create profile vectors based on a base vector and a series of profiles to append."
  [base profiles]
  (->> profiles
    (map vector)
    (cons nil)
    (map #(concat base %))
    (map (comp reverse distinct reverse))
    (distinct)))

;; ## Test Frameworks

(defn find-artifact
  "Return vector with first profile containing the given artifact."
  [artifact-map k artifact]
  (when-let [profiles (get-in artifact-map [k artifact])]
    [(first (keys profiles))]))

(def ^:private frameworks
  "Map of frameworks with detect function, included frameworks and test/autotest tasks."
  {:midje {:detect (fn [artifact-map] 
                     (when-let [midje-plugin (find-artifact artifact-map :plugins 'lein-midje)]
                       (if-let [midje-dep (find-artifact artifact-map :dependencies 'midje)]
                         (distinct (cons :dev (concat midje-dep midje-plugin)))
                         (println "WARN: plugin 'lein-midje' given, but dependency 'midje' not found."))))
           :includes [:clojure.test]
           :test ["midje"]
           :autotest ["midje" ":autotest"]}
   :speclj {:detect (fn [artifact-map] 
                     (when-let [speclj-plugin (find-artifact artifact-map :plugins 'speclj)]
                       (if-let [speclj-dep (find-artifact artifact-map :dependencies 'speclj)]
                         (distinct (cons :dev (concat speclj-dep speclj-plugin)))
                         (println "WARN: plugin 'speclj' given, but dependency 'speclj' not found."))))
           :includes []
           :test ["spec"]
           :autotest ["spec" "-a"]}
   :clojure.test {:detect (constantly [:dev])
                  :includes []
                  :test ["test"]
                  :autotest nil}})

(defn detect-frameworks
  "Return map of frameworks applicable to a given artifact map."
  [artifact-map]
  (reduce
    (fn [r [framework {:keys [detect] :as fw}]]
      (or 
        (when-not (some #(contains? (set (:includes (second %))) framework) r)
          (when-let [profiles (detect artifact-map)]
            (assoc r framework (assoc fw :profiles profiles))))
        r))
    {} frameworks))
    
;; ## Putting it all together!

(defn create-with-profile-string
  "Create profile string from base profiles and profiles-to-test."
  [test-profiles artifact-profiles]
  (->> (combine-profiles test-profiles artifact-profiles)
    (map #(map name %))
    (map #(clojure.string/join "," %))
    (clojure.string/join ":")))

(defn create-task
  "Create vector representing a call of 'with-profile' using the given profile string
   and task (a vector)."
  [profile-string task]
  (when task
    (vec (list* "with-profile" profile-string task))))

(defn create-test-tasks
  "Create map associating a test framework with different Leiningen calls that can be used to test
   the given project."
  [project-map]
  (let [artifacts (project-artifacts project-map)
        artifact-profiles (overwriting-profiles artifacts)
        frameworks (detect-frameworks artifacts)]
    (->>
      (for [[framework {:keys [profiles test autotest]}] frameworks]
        (let [profile-string (create-with-profile-string profiles artifact-profiles)]
          (vector framework
                  {:test (create-task profile-string test)
                   :autotest (create-task (clojure.string/join "," (map name profiles)) autotest)})))
      (into {}))))
