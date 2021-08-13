(ns dev-hartmann.transformer
  (:require [clojure.string :as str]
            [dev-hartmann.formatters :refer [url->str review-request->str review-comment->str change-request->str]]))

(defn- extract-review-state [entry state]
  (first (reverse (sort-by :createdAt (filter #(= state (get % :state)) (get-in entry [:reviews :nodes]))))))

(defn review-request->todo [entry]
  (let [author (get-in entry [:author :login])
        title (:title entry)
        now (java.time.Instant/now)
        created-at (java.time.Instant/parse (:createdAt entry))
        hours-since (.toHours (java.time.Duration/between created-at now))
        state (:state entry)
        url-str (url->str (:url entry))
        review-count (get-in entry [:reviews :totalCount])
        approvals (extract-review-state entry "APPROVED")]
    (->> [(str/trim (review-request->str state author title hours-since review-count approvals))
          (str/trim url-str)]
         (str/join \newline))))

(defn change-request->todo [entry]
  (let [title (:title entry)
        now (java.time.Instant/now)
        url-str (url->str (:url entry))
        change-request (extract-review-state entry "CHANGES_REQUESTED")
        created-at (java.time.Instant/parse (:createdAt change-request))
        hours-since (.toHours (java.time.Duration/between created-at now))
        author (get-in change-request [:author :login])
        comment (review-comment->str (:body change-request))]
    (->> [(str/trim (change-request->str "Changes requested: " author title comment hours-since))
          (str/trim url-str)]
         (str/join \newline))))


