(ns dev-hartmann.gitodo
  (:require [dev-hartmann.config :refer [get-config]]
            [dev-hartmann.formatters :refer :all]
            [dev-hartmann.graphql-connector :as gc]
            [dev-hartmann.transformer :as tr])
  (:gen-class))

(defn -main
  [& args]
  (let [config (get-config)
        open-own (gc/get-items config "author" gc/base-query tr/review-request->todo nil so-no-review "")
        changes-requested (gc/get-items config "author" gc/base-query "review:changes_requested" tr/change-request->todo nil st-change-requested "")
        prs-to-review (gc/get-items config "review-requested" gc/base-query tr/review-request->todo gc/review-group-fn so-review-count st-review-count)]
    (print-todos! "Own PRs:" [open-own changes-requested])
    (print-todos! "PRs to review:" prs-to-review)))
