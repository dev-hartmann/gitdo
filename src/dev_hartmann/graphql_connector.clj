(ns dev-hartmann.graphql-connector
  (:require [cheshire.core :refer [generate-string parse-string]]
            [clj-http.lite.client :as client]
            [clojure.string :as str]))

(def graphql-endpoint "https://api.github.com/graphql")

(def base-query
  "query($searchQuery: String!) {
   search(first: 100, query: $searchQuery, type: ISSUE) {
    nodes {
      ... on PullRequest {
        reviews(first: 100) {
          totalCount 
            nodes {
              state
              createdAt
              body
              author {
                login
              }
            }
          }
        url
        title
        state
        createdAt
        updatedAt
        author {
          login
        }
      }
    }
  }
}")

(defn review-group-fn [reviews-requested section-one section-two]
  (let [grouped-results (group-by #(> (get-in % [:reviews :totalCount]) 0) reviews-requested)]
    {section-one (get grouped-results false)
     section-two (get grouped-results true)}))

(defn- indexed->string [idx item]
  (str (inc idx) ". " item)) /

(defn- generate-security-header [token]
  {"Authorization" (str "bearer " token)})

(defn- generate-request-body [query query-variables]
  (generate-string {:query query
                    :variables query-variables}))

(defn- generate-request [headers body]
  {:body body
   :headers headers
   :accept :json})

(defn- generate-query-variable
  ([querytype user]
   (generate-query-variable querytype user ""))
  ([querytype user review-state]
   (let [query-value (str " is:open is:pr " querytype ":" user " archived:false" " " review-state)]
     (generate-string {:searchQuery query-value}))))

(defn- post-grapqhl-request [endpoint request-options]
  (-> (client/post endpoint request-options)
      :body
      (parse-string true)
      :data
      :search
      :nodes))

(defn get-items
  ([config query-type query transformer group-fn section-one section-two]
   (get-items config query-type query "" transformer group-fn section-one section-two))
  ([config query-type query review-state transformer group-fn section-one section-two]
   (let [security-header (generate-security-header (:token config))
         query-variables (generate-query-variable query-type (:username config) review-state)
         request-body (generate-request-body query query-variables)
         url (if (str/blank? (:alt-url config)) graphql-endpoint (:alt-url config))
         request (generate-request security-header request-body)
         sorted-results (sort-by :createdAt (post-grapqhl-request url request))]
     (if-not (nil? group-fn)
       (let [grouped-results (group-fn sorted-results section-one section-two)]
         (for [entry grouped-results]
           (let [section-start (key entry)
                 formatted-entries (map transformer (val entry))
                 indexed-entries (map #(apply indexed->string %) (map-indexed vector formatted-entries))
                 result (if (seq indexed-entries) indexed-entries ["- nothing to do -"])
                 result-string (conj (into [section-start] result) " ")]
             (str/join \newline result-string))))
       (if (seq sorted-results)
         (str/join \newline (-> (map transformer sorted-results)
                                (conj section-one)
                                vec
                                (conj " ")))
         (str/join \newline [section-one "- nothing to do -" " "]))))))
