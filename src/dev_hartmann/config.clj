(ns dev-hartmann.config
  (:require [clojure.java.io :as io]
            [clojure.string :refer [blank?]]))

(defn- config-file-present? []
  (.exists (io/file (System/getProperty "user.home") "gitdo.edn")))

(defn- generate-config []
  (println "Could not find gitodo.edn in ~/HOME, creating one...")
  (println "Please enter git credentials:")
  (let [home-dir (System/getProperty "user.home")
        _ (println "username: ")
        user-name (read-line)
        _ (println "token: ")
        token (read-line)
        _ (println "enterprise github url:")
        alt-url (read-line)
        config {:username user-name :token token :alt-url alt-url}]   
    (spit (io/file home-dir "gitdo.edn") (.toString config))
    (when (config-file-present?)
      (println "Config successfully written to: ~/" home-dir "/gitdo.edn!\n"))
    config))

(defn get-config []
  (if (config-file-present?)
    (read-string (slurp (io/file (System/getProperty "user.home") "gitdo.edn")))
    (generate-config)))
