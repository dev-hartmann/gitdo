(ns dev-hartmann.gitodo
  (:require [clojure.java.io :as io]
            [dev-hartmann.formatters :refer :all]
            [dev-hartmann.graphql-connector :as gc]
            [dev-hartmann.transformer :as tr])
  (:gen-class))


(defn- config-file-present? []
  (.exists (io/file "gtdo.edn")))

(defn- generate-config []
  (println "please enter git user-name and access-token:")
  (let [user-name (read-line) 
        token (read-line)
        config {:username user-name :token token}]
    (spit "gtdo.edn" (.toString config))
    config))

(defn- get-config []
  (if (config-file-present?)
    (read-string (slurp "gtdo.edn"))
    (generate-config)))

(defn -main
  [& args]
  (let [config (get-config)
        own-prs (gc/get-items config "author" gc/base-query "review:changes_requested" tr/change-request->todo gc/review-group-fn so-no-review st-change-requested)
        prs-to-review (gc/get-items config "review-requested" gc/base-query tr/review-request->todo gc/review-group-fn so-review-count st-review-count)]
    (print-todos! "Own PRs:" own-prs)
    (print-todos! "PRs to review:" prs-to-review)))
