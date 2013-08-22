(ns ^{ :doc "Framework Detection for lein-testem."
       :author "Yannick Scherer" }
  testem.core)

;; ## Utilities

(defn- create-profile-keys
  "Create pairs of `[profile key]` (where key is destined for `get-in`) to access the given 
   field on a project and profile basis (e.g. `:dependencies`, `:plugins`, ...)."
  [project k]
  (->> (keys (:profiles project))
    (map #(vector % [:profiles % k]))
    (cons [nil [k]])))

(defn- create-profile-artifact-map
  "Create nested map of `{ artifact { profile version ... } ... }` using a given key."
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
